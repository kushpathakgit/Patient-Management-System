package com.pm.patientservice.mapper;

import com.pm.patientservice.dto.PatientRequestDTO;
import com.pm.patientservice.dto.PatientResponseDTO;
import com.pm.patientservice.model.Patient;

import java.time.LocalDate;

public class PatientMapper {

    public static PatientResponseDTO toDTO(Patient patient) {
        // Return null immediately if the passed patient object is null
        if (patient == null) {
            return null;
        }

        PatientResponseDTO patientDTO = new PatientResponseDTO();

        // Safely convert ID to String
        if (patient.getId() != null) {
            patientDTO.setId(patient.getId().toString());
        }

        patientDTO.setName(patient.getName());
        patientDTO.setAddress(patient.getAddress());
        patientDTO.setEmail(patient.getEmail());

        // Safely convert LocalDate to String
        if (patient.getDateOfBirth() != null) {
            patientDTO.setDateOfBirth(patient.getDateOfBirth().toString());
        }

        return patientDTO;
    }

    public static Patient toModel(PatientRequestDTO patientResponseDTO) {
        Patient patient = new Patient();
        patient.setName(patientResponseDTO.getName());
        patient.setAddress(patientResponseDTO.getAddress());
        patient.setEmail(patientResponseDTO.getEmail());
        patient.setDateOfBirth(LocalDate.parse(patientResponseDTO.getDateOfBirth()));
        patient.setRegisteredDate(LocalDate.parse(patientResponseDTO.getRegisteredDate()));
        return patient;
    }
}