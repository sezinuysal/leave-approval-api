package com.sezinsu.leaveapproval.service;

import com.sezinsu.leaveapproval.domain.LeaveRequest;
import com.sezinsu.leaveapproval.domain.LeaveStatus;
import com.sezinsu.leaveapproval.dto.CreateLeaveRequest;
import com.sezinsu.leaveapproval.dto.DecisionRequest;
import com.sezinsu.leaveapproval.dto.LeaveRequestResponse;
import com.sezinsu.leaveapproval.exception.NotFoundException;
import com.sezinsu.leaveapproval.repository.LeaveRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

@Service
public class LeaveRequestService {

    private final LeaveRequestRepository repository;
    private final Clock clock;

    public LeaveRequestService(LeaveRequestRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Transactional
    public LeaveRequestResponse create(CreateLeaveRequest request) {
        LeaveRequest leaveRequest = new LeaveRequest(
                request.employeeName(), request.startDate(), request.endDate(), request.reason());
        return LeaveRequestResponse.from(repository.save(leaveRequest));
    }

    @Transactional(readOnly = true)
    public LeaveRequestResponse get(Long id) {
        return LeaveRequestResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public List<LeaveRequestResponse> list(LeaveStatus status) {
        List<LeaveRequest> requests = status == null
                ? repository.findAll()
                : repository.findByStatusOrderByIdAsc(status);
        return requests.stream().map(LeaveRequestResponse::from).toList();
    }

    @Transactional
    public LeaveRequestResponse decide(Long id, DecisionRequest decision) {
        LeaveRequest leaveRequest = find(id);
        leaveRequest.decide(decision.approver(), decision.approved(), decision.comment(), Instant.now(clock));
        return LeaveRequestResponse.from(leaveRequest);
    }

    private LeaveRequest find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Leave request " + id + " not found"));
    }
}
