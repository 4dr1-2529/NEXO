INSERT INTO rol (codigo, nombre, descripcion) VALUES
    ('ADMINISTRADOR', 'Administrador', 'Gestión de la estación'),
    ('OPERADOR', 'Operador', 'Inventario y ajustes autorizados'),
    ('VENDEDOR', 'Vendedor', 'Ventas y consulta de existencias');

INSERT INTO concepto_movimiento (codigo, nombre, tipo) VALUES
    ('COMPRA', 'Compra de combustible', 'EGRESO'),
    ('VENTA', 'Venta de combustible', 'INGRESO');
