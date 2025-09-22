package com.epam.engagement_system.util;

import com.epam.engagement_system.domain.ApplicationUser;
import com.epam.engagement_system.domain.Appointment;
import com.epam.engagement_system.domain.TimeSlot;
import com.epam.engagement_system.dto.appointment.AppointmentCreationRequest;
import com.epam.engagement_system.dto.appointment.AppointmentInformationResponse;

import java.util.Locale;

public class AppointmentUtil {
    public static AppointmentInformationResponse mapToAppointmentInformationDto(Appointment appointment, int historyCount) {
        return new AppointmentInformationResponse(
                appointment.getId(),
                appointment.getStatus().name(),
                appointment.getTimeSlot().getStartTime(),
                appointment.getTimeSlot().getEndTime(),
                appointment.getApplicant().getPhoneNumber(),
                historyCount,
                appointment.getGroomFirstName(),
                appointment.getGroomLastName(),
                appointment.getBrideFirstName(),
                appointment.getBrideLastName(),
                appointment.getWitness1FirstName(),
                appointment.getWitness1LastName(),
                appointment.getWitness2FirstName(),
                appointment.getWitness2LastName(),
                appointment.getWitness3FirstName(),
                appointment.getWitness3LastName(),
                appointment.getNotes(),
                appointment.getCreatedAt(),
                appointment.getDocumentPath(),
                appointment.getRejectionReason()
        );
    }

    public static Appointment toAppointment(AppointmentCreationRequest request, ApplicationUser user,
                                            TimeSlot timeSlot) {
        if (request == null) return null;
        Appointment appointment = new Appointment();

        switch (user.getGender()) {
            case MALE -> {
                appointment.setGroomFirstName(user.getFirstName());
                appointment.setGroomLastName(user.getLastName());
                appointment.setBrideFirstName(normalizeName(request.spouseFirstName()));
                appointment.setBrideLastName(normalizeName(request.spouseLastName()));
            }
            case FEMALE -> {
                appointment.setBrideFirstName(user.getFirstName());
                appointment.setBrideLastName(user.getLastName());
                appointment.setGroomFirstName(normalizeName(request.spouseFirstName()));
                appointment.setGroomLastName(normalizeName(request.spouseLastName()));
            }
        }
        appointment.setApplicant(user);
        appointment.setTimeSlot(timeSlot);
        appointment.setNotes(request.notes());

        appointment.setWitness1FirstName(normalizeName(request.witnesses().getFirst().firstName()));
        appointment.setWitness1LastName(normalizeName(request.witnesses().getFirst().lastName()));
        appointment.setWitness2FirstName(normalizeName(request.witnesses().get(1).firstName()));
        appointment.setWitness2LastName(normalizeName(request.witnesses().get(1).lastName()));

        if (request.witnesses().size() > 2) {
            appointment.setWitness3FirstName(normalizeName(request.witnesses().get(2).firstName()));
            appointment.setWitness3LastName(normalizeName(request.witnesses().get(2).lastName()));
        }
        appointment.setDocumentPath("/temp");

        return appointment;
    }

    private static String normalizeName(String name) {
        name = name.trim().toLowerCase(Locale.ROOT);
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }
}
