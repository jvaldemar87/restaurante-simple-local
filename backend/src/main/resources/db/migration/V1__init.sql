CREATE TABLE IF NOT EXISTS usuarios (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre TEXT NOT NULL,
    username TEXT NOT NULL UNIQUE,
    password TEXT NOT NULL,
    rol TEXT NOT NULL CHECK(rol IN ('MESERO', 'CAJERO', 'ADMIN')),
    activo INTEGER NOT NULL DEFAULT 1,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS categorias (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre TEXT NOT NULL,
    orden INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS productos (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre TEXT NOT NULL,
    precio REAL NOT NULL,
    categoria_id INTEGER NOT NULL,
    disponible INTEGER NOT NULL DEFAULT 1,
    FOREIGN KEY (categoria_id) REFERENCES categorias(id)
);

CREATE TABLE IF NOT EXISTS mesas (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    numero INTEGER NOT NULL UNIQUE,
    estado TEXT NOT NULL DEFAULT 'LIBRE' CHECK(estado IN ('LIBRE', 'OCUPADA')),
    capacidad INTEGER NOT NULL DEFAULT 4,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS comensales (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre TEXT NOT NULL,
    mesa_id INTEGER NOT NULL,
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (mesa_id) REFERENCES mesas(id)
);

CREATE TABLE IF NOT EXISTS pedidos (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    comensal_id INTEGER NOT NULL,
    fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    estado TEXT NOT NULL DEFAULT 'ACTIVO' CHECK(estado IN ('ACTIVO', 'CERRADO')),
    total REAL DEFAULT 0,
    FOREIGN KEY (comensal_id) REFERENCES comensales(id)
);

CREATE TABLE IF NOT EXISTS detalle_pedidos (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    pedido_id INTEGER NOT NULL,
    producto_id INTEGER NOT NULL,
    cantidad INTEGER NOT NULL DEFAULT 1,
    precio_unitario REAL NOT NULL,
    subtotal REAL NOT NULL,
    FOREIGN KEY (pedido_id) REFERENCES pedidos(id),
    FOREIGN KEY (producto_id) REFERENCES productos(id)
);

CREATE TABLE IF NOT EXISTS categorias_insumo (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS insumos (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre TEXT NOT NULL,
    cantidad REAL NOT NULL,
    unidad TEXT NOT NULL DEFAULT 'pza',
    fecha_ingreso TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    precio_unitario REAL NOT NULL,
    categoria_insumo_id INTEGER NOT NULL,
    FOREIGN KEY (categoria_insumo_id) REFERENCES categorias_insumo(id)
);

CREATE TABLE IF NOT EXISTS categorias_pago (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS pagos (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    concepto TEXT NOT NULL,
    monto REAL NOT NULL,
    fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    categoria_pago_id INTEGER NOT NULL,
    evidencia_imagen TEXT,
    observaciones TEXT,
    FOREIGN KEY (categoria_pago_id) REFERENCES categorias_pago(id)
);

-- Seed data: categorias por defecto
INSERT OR IGNORE INTO categorias (id, nombre, orden) VALUES (1, 'Comidas', 1);
INSERT OR IGNORE INTO categorias (id, nombre, orden) VALUES (2, 'Bebidas', 2);

-- Seed data: categorias de insumo
INSERT OR IGNORE INTO categorias_insumo (id, nombre) VALUES (1, 'Alimentos y bebidas');
INSERT OR IGNORE INTO categorias_insumo (id, nombre) VALUES (2, 'Materiales de cocina');
INSERT OR IGNORE INTO categorias_insumo (id, nombre) VALUES (3, 'Insumos operativos');
INSERT OR IGNORE INTO categorias_insumo (id, nombre) VALUES (4, 'Otros');

-- Seed data: categorias de pago
INSERT OR IGNORE INTO categorias_pago (id, nombre) VALUES (1, 'Sueldos');
INSERT OR IGNORE INTO categorias_pago (id, nombre) VALUES (2, 'Renta');
INSERT OR IGNORE INTO categorias_pago (id, nombre) VALUES (3, 'Servicios');
INSERT OR IGNORE INTO categorias_pago (id, nombre) VALUES (4, 'Otros');

-- Seed data: mesas por defecto
INSERT OR IGNORE INTO mesas (id, numero) VALUES (1, 1);
INSERT OR IGNORE INTO mesas (id, numero) VALUES (2, 2);
INSERT OR IGNORE INTO mesas (id, numero) VALUES (3, 3);
INSERT OR IGNORE INTO mesas (id, numero) VALUES (4, 4);
