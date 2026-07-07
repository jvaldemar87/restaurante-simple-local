CREATE TABLE IF NOT EXISTS historial_pedidos (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre_comensal TEXT NOT NULL,
    nombre_producto TEXT NOT NULL,
    cantidad INTEGER NOT NULL,
    precio_unitario REAL NOT NULL,
    subtotal REAL NOT NULL,
    mesa_numero INTEGER NOT NULL,
    fecha_pedido TIMESTAMP NOT NULL,
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- SQLite no permite ALTER COLUMN, se recrea la tabla
CREATE TABLE pedidos_v2 (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    comensal_id INTEGER,
    mesa_id INTEGER NOT NULL,
    fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    estado TEXT NOT NULL DEFAULT 'ACTIVO' CHECK(estado IN ('ACTIVO', 'CERRADO')),
    total REAL DEFAULT 0,
    FOREIGN KEY (comensal_id) REFERENCES comensales(id),
    FOREIGN KEY (mesa_id) REFERENCES mesas(id)
);

INSERT INTO pedidos_v2 (id, comensal_id, fecha, estado, total)
SELECT id, comensal_id, fecha, estado, total FROM pedidos;

UPDATE pedidos_v2 SET mesa_id = (SELECT mesa_id FROM comensales WHERE comensales.id = pedidos_v2.comensal_id)
WHERE mesa_id IS NULL;

DROP TABLE pedidos;
ALTER TABLE pedidos_v2 RENAME TO pedidos;
