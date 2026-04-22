package kz.logisto.lguserservice.data.dto.client;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateClientDto(
    @Size(max = 255) String firstName,
    @Size(max = 255) String lastName,
    @Size(max = 255) String middleName,
    LocalDate dateOfBirth,
    @Size(max = 255) @Email String email,
    @Size(max = 15) String phoneNumber,
    @DecimalMin(value = "0.0") @DecimalMax(value = "100.0") @Digits(integer = 3, fraction = 2) BigDecimal personalDiscount
) { }
