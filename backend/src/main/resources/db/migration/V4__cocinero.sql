-- Añadir COCINERO al CHECK de usuarios
CREATE TABLE usuarios_v4 (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre TEXT NOT NULL,
    username TEXT NOT NULL UNIQUE,
    password TEXT NOT NULL,
    rol TEXT NOT NULL CHECK(rol IN ('MESERO', 'CAJERO', 'ADMIN', 'COCINERO')),
    activo INTEGER NOT NULL DEFAULT 1,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
INSERT INTO usuarios_v4 SELECT * FROM usuarios;
DROP TABLE usuarios;
ALTER TABLE usuarios_v4 RENAME TO usuarios;

-- Añadir fecha_comanda y entregado a pedidos
CREATE TABLE pedidos_v4 (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    comensal_id INTEGER,
    mesa_id INTEGER NOT NULL,
    fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    estado TEXT NOT NULL DEFAULT 'ACTIVO' CHECK(estado IN ('ACTIVO', 'COMIENDO', 'CERRADO')),
    total REAL DEFAULT 0,
    fecha_comanda TIMESTAMP,
    entregado INTEGER NOT NULL DEFAULT 0,
    FOREIGN KEY (comensal_id) REFERENCES comensales(id),
    FOREIGN KEY (mesa_id) REFERENCES mesas(id)
);
INSERT INTO pedidos_v4 (id, comensal_id, mesa_id, fecha, estado, total)
    SELECT id, comensal_id, mesa_id, fecha, estado, total FROM pedidos;
UPDATE pedidos_v4 SET entregado = 1 WHERE estado = 'CERRADO';
DROP TABLE pedidos;
ALTER TABLE pedidos_v4 RENAME TO pedidos;

-- Tabla de configuraciones
CREATE TABLE IF NOT EXISTS configuracion (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    clave TEXT NOT NULL UNIQUE,
    valor TEXT NOT NULL
);
INSERT OR IGNORE INTO configuracion (clave, valor) VALUES ('tiempo_tolerancia_minutos', '30');
