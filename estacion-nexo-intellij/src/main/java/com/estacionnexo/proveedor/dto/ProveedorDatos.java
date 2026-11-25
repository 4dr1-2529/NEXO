package com.estacionnexo.proveedor.dto;

import jakarta.validation.constraints.*;

public record ProveedorDatos(@NotBlank @Pattern(regexp = "[0-9]{11}") String ruc, @NotBlank @Size(max = 120) String razonSocial, @Size(max = 80) String contacto, @Pattern(regexp = "(?:[0-9]{9})?", message = "El teléfono debe tener 9 dígitos.") String telefono, @Email @Size(max = 120) String correo, @Size(max = 200) String direccion) {}
