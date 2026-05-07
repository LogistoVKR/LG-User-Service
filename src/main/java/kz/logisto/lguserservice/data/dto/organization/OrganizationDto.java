package kz.logisto.lguserservice.data.dto.organization;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record OrganizationDto(
    @Size(max = 255) @NotBlank String name,
    @Size(max = 255) String description,
    @Size(max = 255) String ozonApiKey) { }
