-- Permite ingresos/egresos manuales autorizados (sin compra ni venta).
-- Compatible con MariaDB 10.4 (DROP CONSTRAINT, no DROP CHECK).

SET @chk := (
    SELECT cc.CONSTRAINT_NAME
    FROM information_schema.CHECK_CONSTRAINTS cc
    INNER JOIN information_schema.TABLE_CONSTRAINTS tc
        ON cc.CONSTRAINT_SCHEMA = tc.CONSTRAINT_SCHEMA
       AND cc.CONSTRAINT_NAME = tc.CONSTRAINT_NAME
    WHERE cc.CONSTRAINT_SCHEMA = DATABASE()
      AND tc.TABLE_NAME = 'movimiento_caja'
      AND cc.CHECK_CLAUSE LIKE '%id_venta%'
    LIMIT 1
);

SET @ddl := IF(@chk IS NULL,
    'SELECT 1',
    CONCAT('ALTER TABLE movimiento_caja DROP CONSTRAINT `', @chk, '`'));
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

ALTER TABLE movimiento_caja
    ADD CONSTRAINT movimiento_caja_origen_chk CHECK (
        (id_compra IS NULL OR id_venta IS NULL)
        AND (id_compra IS NULL OR tipo = 'EGRESO')
        AND (id_venta IS NULL OR tipo = 'INGRESO')
        AND tipo IN ('INGRESO', 'EGRESO')
    );

ALTER TABLE movimiento_caja
    ADD COLUMN solicitud CHAR(36) NULL UNIQUE AFTER fecha_hora;

INSERT INTO concepto_movimiento (codigo, nombre, tipo, estado)
SELECT 'MAN-EGR', 'Egreso manual autorizado', 'EGRESO', 'Activo'
WHERE NOT EXISTS (SELECT 1 FROM concepto_movimiento WHERE codigo = 'MAN-EGR');

INSERT INTO concepto_movimiento (codigo, nombre, tipo, estado)
SELECT 'MAN-ING', 'Ingreso manual autorizado', 'INGRESO', 'Activo'
WHERE NOT EXISTS (SELECT 1 FROM concepto_movimiento WHERE codigo = 'MAN-ING');
