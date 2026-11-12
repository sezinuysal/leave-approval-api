package com.sezinsu.leaveapproval.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class LeaveRequestControllerTest {

    private static final String VALID_REQUEST = """
            {"employeeName":"Ayse","startDate":"2026-03-02","endDate":"2026-03-06","reason":"Holiday"}
            """;

    @Autowired
    private MockMvc mockMvc;

    /** Creates a request and returns its id. */
    private long createRequest() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/leave-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REQUEST))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andReturn();
        String location = result.getResponse().getHeader("Location");
        return Long.parseLong(location.substring(location.lastIndexOf('/') + 1));
    }

    private void decide(long id, String approver, boolean approved, int expectedStatus) throws Exception {
        mockMvc.perform(post("/api/leave-requests/{id}/decisions", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"approver\":\"" + approver + "\",\"approved\":" + approved + "}"))
                .andExpect(status().is(expectedStatus));
    }

    @Test
    void createRequest_returnsPendingRequest() throws Exception {
        mockMvc.perform(post("/api/leave-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REQUEST))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.currentStep").value("MANAGER"))
                .andExpect(jsonPath("$.employeeName").value("Ayse"));
    }

    @Test
    void createRequest_withBlankEmployeeName_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/leave-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"employeeName\":\"\",\"startDate\":\"2026-03-02\",\"endDate\":\"2026-03-06\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.employeeName").exists());
    }

    @Test
    void createRequest_withEndDateBeforeStartDate_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/leave-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"employeeName\":\"Ayse\",\"startDate\":\"2026-03-06\",\"endDate\":\"2026-03-02\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void fullApprovalFlow_endsApproved() throws Exception {
        long id = createRequest();

        decide(id, "manager", true, 200);
        decide(id, "hr", true, 200);

        mockMvc.perform(get("/api/leave-requests/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.decisions.length()").value(2))
                .andExpect(jsonPath("$.decisions[0].step").value("MANAGER"))
                .andExpect(jsonPath("$.decisions[1].step").value("HR"));
    }

    @Test
    void rejection_endsRejected_andFurtherDecisionsReturnConflict() throws Exception {
        long id = createRequest();

        decide(id, "manager", false, 200);
        decide(id, "hr", true, 409);

        mockMvc.perform(get("/api/leave-requests/{id}", id))
                .andExpect(jsonPath("$.status").value("REJECTED"));
    }

    @Test
    void unknownRequest_returnsNotFound() throws Exception {
        mockMvc.perform(get("/api/leave-requests/{id}", 999_999))
                .andExpect(status().isNotFound());
        decide(999_999, "manager", true, 404);
    }

    @Test
    void list_canBeFilteredByStatus() throws Exception {
        long approvedId = createRequest();
        decide(approvedId, "manager", true, 200);
        decide(approvedId, "hr", true, 200);

        mockMvc.perform(get("/api/leave-requests").param("status", "APPROVED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == " + approvedId + ")]").exists())
                .andExpect(jsonPath("$[?(@.status != 'APPROVED')]").isEmpty());
    }
}
