CREATE TABLE bitacora (
    id_bitacora BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(30),
    rol VARCHAR(20),
    accion VARCHAR(40) NOT NULL,
    modulo VARCHAR(40) NOT NULL,
    registro_afectado VARCHAR(80),
    valores_anteriores VARCHAR(1000),
    valores_nuevos VARCHAR(1000),
    resultado VARCHAR(20) NOT NULL,
    fecha_hora DATETIME NOT NULL,
    INDEX (fecha_hora),
    INDEX (modulo, accion),
    INDEX (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
