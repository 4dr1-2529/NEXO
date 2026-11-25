-- Diccionario de datos de Estación Nexo, Informe de Avance 1, grupo G1.

CREATE TABLE categoria (
    id_categoria BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE,
    descripcion VARCHAR(200),
    estado VARCHAR(10) NOT NULL DEFAULT 'Activo' CHECK (estado IN ('Activo','Inactivo')),
    fecha_creacion DATETIME NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE producto (
    id_producto BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    codigo_producto VARCHAR(10) NOT NULL UNIQUE,
    id_categoria BIGINT NOT NULL,
    nombre VARCHAR(60) NOT NULL,
    unidad_medida VARCHAR(10) NOT NULL DEFAULT 'L',
    precio_venta DECIMAL(12,2) NOT NULL CHECK (precio_venta > 0),
    stock_actual DECIMAL(12,3) NOT NULL DEFAULT 0 CHECK (stock_actual >= 0),
    estado VARCHAR(10) NOT NULL DEFAULT 'Activo' CHECK (estado IN ('Activo','Inactivo')),
    UNIQUE (id_categoria,nombre),
    FOREIGN KEY (id_categoria) REFERENCES categoria(id_categoria)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE proveedor (
    id_proveedor BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    ruc CHAR(11) NOT NULL UNIQUE,
    razon_social VARCHAR(120) NOT NULL,
    contacto VARCHAR(80), telefono VARCHAR(15), correo VARCHAR(120), direccion VARCHAR(200),
    estado VARCHAR(10) NOT NULL DEFAULT 'Activo' CHECK (estado IN ('Activo','Inactivo'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE empleado (
    id_empleado BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    dni CHAR(8) NOT NULL UNIQUE,
    nombres VARCHAR(60) NOT NULL, apellidos VARCHAR(60) NOT NULL,
    cargo VARCHAR(40) NOT NULL, telefono VARCHAR(15) NOT NULL, fecha_ingreso DATE,
    estado VARCHAR(10) NOT NULL DEFAULT 'Activo' CHECK (estado IN ('Activo','Inactivo'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE rol (
    id_rol BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(20) NOT NULL UNIQUE,
    nombre VARCHAR(40) NOT NULL UNIQUE, descripcion VARCHAR(200) NOT NULL,
    estado VARCHAR(10) NOT NULL DEFAULT 'Activo' CHECK (estado IN ('Activo','Inactivo'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE usuario (
    id_usuario BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    id_empleado BIGINT NOT NULL UNIQUE, id_rol BIGINT NOT NULL,
    username VARCHAR(30) NOT NULL UNIQUE, password_hash VARCHAR(100) NOT NULL,
    estado VARCHAR(10) NOT NULL DEFAULT 'Activo' CHECK (estado IN ('Activo','Inactivo')),
    fecha_creacion DATETIME NOT NULL, ultimo_acceso DATETIME,
    FOREIGN KEY (id_empleado) REFERENCES empleado(id_empleado),
    FOREIGN KEY (id_rol) REFERENCES rol(id_rol)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE compra (
    id_compra BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    codigo_compra VARCHAR(12) NOT NULL UNIQUE,
    id_proveedor BIGINT NOT NULL, observacion VARCHAR(200),
    id_usuario BIGINT NOT NULL,
    fecha_hora DATETIME NOT NULL, total DECIMAL(12,2) NOT NULL CHECK (total > 0),
    estado VARCHAR(20) NOT NULL CHECK (estado IN ('Pendiente','Confirmada')),
    solicitud CHAR(36) UNIQUE,
    INDEX (fecha_hora),
    FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario),
    FOREIGN KEY (id_proveedor) REFERENCES proveedor(id_proveedor)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE detalle_compra (
    id_detalle_compra BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    id_compra BIGINT NOT NULL, id_producto BIGINT NOT NULL,
    cantidad_litros DECIMAL(12,3) NOT NULL CHECK (cantidad_litros > 0),
    precio_compra DECIMAL(12,2) NOT NULL CHECK (precio_compra > 0),
    subtotal DECIMAL(12,2) NOT NULL CHECK (subtotal > 0),
    FOREIGN KEY (id_compra) REFERENCES compra(id_compra),
    FOREIGN KEY (id_producto) REFERENCES producto(id_producto)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE venta (
    id_venta BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    codigo_venta VARCHAR(12) NOT NULL UNIQUE,
    id_usuario BIGINT NOT NULL,
    fecha_hora DATETIME NOT NULL, total DECIMAL(12,2) NOT NULL CHECK (total > 0),
    estado VARCHAR(20) NOT NULL CHECK (estado IN ('Pendiente','Confirmada')),
    solicitud CHAR(36) UNIQUE,
    INDEX (fecha_hora),
    FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE detalle_venta (
    id_detalle_venta BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    id_venta BIGINT NOT NULL, id_producto BIGINT NOT NULL,
    cantidad_litros DECIMAL(12,3) NOT NULL CHECK (cantidad_litros > 0),
    precio_unitario DECIMAL(12,2) NOT NULL CHECK (precio_unitario > 0),
    subtotal DECIMAL(12,2) NOT NULL CHECK (subtotal > 0),
    FOREIGN KEY (id_venta) REFERENCES venta(id_venta),
    FOREIGN KEY (id_producto) REFERENCES producto(id_producto)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE movimiento_inventario (
    id_movimiento_inventario BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    id_producto BIGINT NOT NULL, id_usuario BIGINT NOT NULL,
    id_detalle_compra BIGINT UNIQUE, id_detalle_venta BIGINT UNIQUE,
    tipo VARCHAR(10) NOT NULL CHECK (tipo IN ('ENTRADA','SALIDA')),
    cantidad_litros DECIMAL(12,3) NOT NULL CHECK (cantidad_litros > 0),
    fecha_hora DATETIME NOT NULL, motivo VARCHAR(200) NOT NULL, solicitud CHAR(36) UNIQUE,
    INDEX (fecha_hora),
    CHECK (id_detalle_compra IS NULL OR (id_detalle_venta IS NULL AND tipo='ENTRADA')),
    CHECK (id_detalle_venta IS NULL OR tipo='SALIDA'),
    FOREIGN KEY (id_producto) REFERENCES producto(id_producto),
    FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario),
    FOREIGN KEY (id_detalle_compra) REFERENCES detalle_compra(id_detalle_compra),
    FOREIGN KEY (id_detalle_venta) REFERENCES detalle_venta(id_detalle_venta)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE concepto_movimiento (
    id_concepto BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(10) NOT NULL UNIQUE, nombre VARCHAR(60) NOT NULL,
    tipo VARCHAR(10) NOT NULL CHECK (tipo IN ('INGRESO','EGRESO')),
    estado VARCHAR(10) NOT NULL DEFAULT 'Activo' CHECK (estado IN ('Activo','Inactivo'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE caja_diaria (
    id_caja BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    fecha DATE NOT NULL UNIQUE,
    saldo_apertura DECIMAL(12,2) NOT NULL CHECK (saldo_apertura >= 0),
    saldo_cierre DECIMAL(12,2), estado VARCHAR(10) NOT NULL CHECK (estado IN ('ABIERTA','CERRADA')),
    id_usuario_apertura BIGINT NOT NULL, id_usuario_cierre BIGINT,
    fecha_apertura DATETIME NOT NULL, fecha_cierre DATETIME,
    CHECK ((estado='ABIERTA' AND saldo_cierre IS NULL AND id_usuario_cierre IS NULL AND fecha_cierre IS NULL)
        OR (estado='CERRADA' AND saldo_cierre IS NOT NULL AND id_usuario_cierre IS NOT NULL AND fecha_cierre IS NOT NULL)),
    FOREIGN KEY (id_usuario_apertura) REFERENCES usuario(id_usuario),
    FOREIGN KEY (id_usuario_cierre) REFERENCES usuario(id_usuario)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE movimiento_caja (
    id_movimiento_caja BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    codigo_movimiento VARCHAR(12) NOT NULL UNIQUE,
    id_caja BIGINT NOT NULL, id_concepto BIGINT NOT NULL, id_usuario BIGINT NOT NULL,
    id_compra BIGINT UNIQUE, id_venta BIGINT UNIQUE,
    tipo VARCHAR(10) NOT NULL, monto DECIMAL(12,2) NOT NULL CHECK (monto > 0),
    descripcion VARCHAR(200) NOT NULL, fecha_hora DATETIME NOT NULL,
    CHECK ((tipo='INGRESO' AND id_venta IS NOT NULL AND id_compra IS NULL)
        OR (tipo='EGRESO' AND id_compra IS NOT NULL AND id_venta IS NULL)),
    INDEX (id_caja,tipo),
    FOREIGN KEY (id_caja) REFERENCES caja_diaria(id_caja),
    FOREIGN KEY (id_concepto) REFERENCES concepto_movimiento(id_concepto),
    FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario),
    FOREIGN KEY (id_compra) REFERENCES compra(id_compra),
    FOREIGN KEY (id_venta) REFERENCES venta(id_venta)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE asistencia (
    id_asistencia BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    id_empleado BIGINT NOT NULL, fecha DATE NOT NULL, hora_entrada TIME NOT NULL,
    hora_salida TIME, estado VARCHAR(10) NOT NULL CHECK (estado IN ('PRESENTE','FALTA')),
    observacion VARCHAR(200),
    UNIQUE (id_empleado,fecha),
    CHECK (hora_salida IS NULL OR hora_salida > hora_entrada),
    FOREIGN KEY (id_empleado) REFERENCES empleado(id_empleado)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
