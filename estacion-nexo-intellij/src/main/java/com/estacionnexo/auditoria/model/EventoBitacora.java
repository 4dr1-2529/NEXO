package com.estacionnexo.auditoria.model;

import java.time.LocalDateTime;

public record EventoBitacora(long id, String username, String rol, String accion, String modulo,
                             String registroAfectado, String valoresAnteriores, String valoresNuevos,
                             String resultado, LocalDateTime fechaHora) {}
