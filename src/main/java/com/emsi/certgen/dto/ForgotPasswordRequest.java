package com.emsi.certgen.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ForgotPasswordRequest {
    @Email(message = "Email invalide")
    @NotBlank(message = "L'email est requis")
    private String email;
}
