package com.sezinsu.leaveapproval.controller;

import com.sezinsu.leaveapproval.domain.LeaveStatus;
import com.sezinsu.leaveapproval.dto.CreateLeaveRequest;
import com.sezinsu.leaveapproval.dto.DecisionRequest;
import com.sezinsu.leaveapproval.dto.LeaveRequestResponse;
import com.sezinsu.leaveapproval.service.LeaveRequestService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/leave-requests")
public class LeaveRequestController {

    private final LeaveRequestService service;

    public LeaveRequestController(LeaveRequestService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<LeaveRequestResponse> create(@Valid @RequestBody CreateLeaveRequest request) {
        LeaveRequestResponse created = service.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping
    public List<LeaveRequestResponse> list(@RequestParam(required = false) LeaveStatus status) {
        return service.list(status);
    }

    @GetMapping("/{id}")
    public LeaveRequestResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    @PostMapping("/{id}/decisions")
    public LeaveRequestResponse decide(@PathVariable Long id, @Valid @RequestBody DecisionRequest decision) {
        return service.decide(id, decision);
    }
}
