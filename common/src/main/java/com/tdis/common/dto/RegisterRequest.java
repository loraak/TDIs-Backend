package com.tdis.common.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "La matricula es requerida")
    @Size(min = 8, message = "La matricula debe tener al menos 8 digitos")
    private String matricula;

    @NotBlank(message = "El correo institucional es requerido")
    @Email(message = "El correo debe ser valido")
    private String email;

    @NotBlank(message = "La contrasena es requerida")
    @Size(min = 6, message = "La contrasena debe tener al menos 6 caracteres")
    private String password;

    @NotBlank(message = "El nombre es requerido")
    private String nombre;

    @NotBlank(message = "Los apellidos son requeridos")
    private String apellidos;

    private String tipo;
    private String division;
    private String programa;
    private String grupo;
    private String cuatrimestre;
    private String turno;
    private String tutor;
}
