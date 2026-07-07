CREATE TABLE pedidos_v3 (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    comensal_id INTEGER,
    mesa_id INTEGER NOT NULL,
    fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    estado TEXT NOT NULL DEFAULT 'ACTIVO' CHECK(estado IN ('ACTIVO', 'COMIENDO', 'CERRADO')),
    total REAL DEFAULT 0,
    FOREIGN KEY (comensal_id) REFERENCES comensales(id),
    FOREIGN KEY (mesa_id) REFERENCES mesas(id)
);

INSERT INTO pedidos_v3 (id, comensal_id, mesa_id, fecha, estado, total)
SELECT id, comensal_id, mesa_id, fecha, estado, total FROM pedidos;

DROP TABLE pedidos;
ALTER TABLE pedidos_v3 RENAME TO pedidos;
