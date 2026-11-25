UPDATE producto p JOIN categoria c ON c.id_categoria = p.id_categoria
SET p.estado = 'Inactivo'
WHERE c.nombre = 'Gasoholes' AND p.estado = 'Activo';

UPDATE categoria SET estado = 'Inactivo'
WHERE nombre = 'Gasoholes' AND estado = 'Activo';
