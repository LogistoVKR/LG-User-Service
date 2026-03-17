package kz.logisto.lguserservice.data.dto.client;

import java.time.LocalDate;

public record ClientFilterDto(String firstName, String lastName, LocalDate dateOfBirth) { }
