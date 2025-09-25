package com.epam.engagement_system.security;

import com.epam.engagement_system.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("checkPermission")
@RequiredArgsConstructor
public class PermissionCheckService {
    private final AppointmentRepository appointmentRepository;

    @Transactional(readOnly = true)
    public boolean isAppointmentOwner(UserPrincipal userPrincipal, Long appointmentId) {
        return appointmentRepository.findById(appointmentId)
                .map(appointment -> appointment.getApplicant().getId().equals(userPrincipal.getId()))
                .orElse(false);
    }
}
