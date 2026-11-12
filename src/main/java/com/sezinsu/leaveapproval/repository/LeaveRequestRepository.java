package com.sezinsu.leaveapproval.repository;

import com.sezinsu.leaveapproval.domain.LeaveRequest;
import com.sezinsu.leaveapproval.domain.LeaveStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    List<LeaveRequest> findByStatusOrderByIdAsc(LeaveStatus status);
}
