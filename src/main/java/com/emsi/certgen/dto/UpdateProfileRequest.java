package com.emsi.certgen.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateProfileRequest {

    /* Nom complet — obligatoire */
    @NotBlank(message = "Le nom complet est requis")
    private String fullName;

    /* Email — obligatoire */
    @Email(message = "Email invalide")
    @NotBlank(message = "L'email est requis")
    private String email;

    /* Username — optionnel, modifiable uniquement par le super admin */
    private String username;
}
