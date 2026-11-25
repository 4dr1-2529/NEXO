-- Estructural: categoría Gases vehiculares y unidades de códigos conocidos.
-- No inserta productos, precios ni existencias.

INSERT INTO categoria (nombre, descripcion, estado, fecha_creacion)
SELECT 'Gases vehiculares',
       'GLP para uso automotor (gal) y GNV comprimido (m3). No mezclar cantidades entre unidades distintas.',
       'Activo',
       UTC_TIMESTAMP()
WHERE NOT EXISTS (SELECT 1 FROM categoria WHERE nombre = 'Gases vehiculares');

UPDATE producto SET unidad_medida = 'L'
WHERE codigo_producto IN ('GAS-REG', 'GAS-PRE', 'DIE-B5S50')
  AND unidad_medida <> 'L';

UPDATE producto SET unidad_medida = 'gal'
WHERE codigo_producto = 'GLP-AUTO'
  AND unidad_medida <> 'gal';

UPDATE producto SET unidad_medida = 'm3'
WHERE codigo_producto = 'GNV-C'
  AND unidad_medida <> 'm3';

-- Gasoholes / GHO-* permanecen inactivos si existen (sin borrar historial).
UPDATE producto p
JOIN categoria c ON c.id_categoria = p.id_categoria
SET p.estado = 'Inactivo'
WHERE p.estado = 'Activo'
  AND (c.nombre = 'Gasoholes' OR p.codigo_producto IN ('GHO-REG', 'GHO-PRE'));

UPDATE categoria
SET estado = 'Inactivo'
WHERE nombre = 'Gasoholes' AND estado = 'Activo';
