package com.estacionnexo.empleado.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record EmpleadoDatos(@NotBlank @Pattern(regexp = "[0-9]{8}", message = "El DNI debe tener 8 dígitos.") String dni,
                           @NotBlank @Size(max = 60) String nombres,
                           @NotBlank @Size(max = 60) String apellidos,
                           @NotBlank @Size(max = 40) String cargo,
                           @NotBlank @Pattern(regexp = "[0-9]{9}", message = "El teléfono debe tener 9 dígitos.") String telefono,
                           LocalDate fechaIngreso) {}
