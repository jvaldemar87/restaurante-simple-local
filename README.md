# Restaurante App

[![Java](https://img.shields.io/badge/Java-17-red)](https://adoptium.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7-brightgreen)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-18-blue)](https://react.dev/)
[![SQLite](https://img.shields.io/badge/SQLite-3-lightgrey)](https://www.sqlite.org/)
[![License](https://img.shields.io/badge/licencia-Interna-orange)]()

App de gestión integral para restaurantes pequeños. Funciona 100% offline con roles de Mesero, Cajero, Cocinero y Administrador. Backend Java Spring Boot + SQLite, frontend React. Incluye toma de pedidos, inventario, pagos, vista de cocina con comandas en tiempo real, respaldos de base de datos y 12 reportes PDF con gráficos.

## Características

### Mesero
- Visualización de mesas con estado (libre/ocupada)
- Gestión de comensales por mesa
- Toma de pedidos con carrito por comensal
- Impresión de comandas y tickets
- Cierre de cuenta

### Cajero
- Vista de solo lectura de mesas y pedidos
- Impresión de ticket
- Cierre de cuenta de mesa

### Cocinero
- Visualización de comandas como tarjetas POST-IT
- Agrupación de productos idénticos por comanda
- Sistema de urgencia con alerta sonora (beep triple)
- Ordenamiento por tiempo de espera (urgentes primero)
- Entrega de comandas con un toque (botón X)
- Tiempo de tolerancia y alerta configurables por admin

### Administrador
- CRUD completo de mesas, productos y categorías
- Control de inventario (insumos) con fecha editable
- Gestión de pagos (sueldos, renta, servicios) con fecha editable y evidencia fotográfica
- 12 reportes PDF con gráficos (ventas, insumos, pagos, KPIs, tendencias, estacionalidad, horas pico, etc.)
- Respaldo y restauración de datos (exportación/importación como ZIP, con opción de incluir imágenes)
- Reinicio automático del servidor tras importar un respaldo
- Validación de fechas (no se permiten fechas futuras en ningún formulario)
- Configuración de tiempo de tolerancia e intervalo de alerta para cocina

## Stack tecnológico

| Capa          | Tecnología                        |
|---------------|-----------------------------------|
| Backend       | Java 17 + Spring Boot 2.7         |
| Base de datos | SQLite (archivo local)            |
| Frontend      | React 18 + React Router 6 + Vite  |
| PDF           | OpenPDF + JFreeChart (gráficos)   |
| Autenticación | JWT + Spring Security             |

## Capturas de pantalla

<!-- Agrega tus capturas en docs/screenshots/ y descomenta esta sección -->
<!--
| Mesero | Cajero | Admin |
|--------|--------|-------|
| ![Mesero](docs/screenshots/mesero.png) | ![Cajero](docs/screenshots/cajero.png) | ![Admin](docs/screenshots/admin.png) |
-->

## Requisitos

- Java 17+ (JDK)
- Maven 3.8+
- Node.js 18+ (solo para desarrollo)

## Instalación y ejecución

### Producción (JAR único)

```bash
cd backend
mvn clean package -DskipTests
java -jar target/restaurante-backend-1.0.0.jar
```

La aplicación estará disponible en **http://localhost:8080**

### Desarrollo

```bash
# Backend (terminal 1)
cd backend
mvn spring-boot:run

# Frontend (terminal 2)
cd frontend
npm install
npm run dev
```

El frontend corre en **http://localhost:3000** con proxy al backend en :8080.

### Build script (Windows PowerShell)

```powershell
.\build.ps1
```

## Usuarios por defecto

Los usuarios se configuran en el archivo `backend/src/main/resources/usuarios` (sin extensión).
Cada línea tiene el formato `USUARIO,CONTRASEÑA,ROL`. Al iniciar el servidor se cargan automáticamente.

**Contenido por defecto:**

| Usuario   | Contraseña   | Rol           |
|-----------|-------------|---------------|
| `admin`   | `admin123`   | Administrador |
| `mesero`  | `mesero123`  | Mesero        |
| `cajero`  | `cajero123`  | Cajero        |
| `cocinero`| `cocinero123`| Cocinero      |

Para cambiar una contraseña, edita el archivo y reinicia el servidor.

## Estructura del proyecto

```
RESTAURANTE/
├── backend/                    # Spring Boot API REST
│   ├── src/main/java/com/restaurante/
│   │   ├── config/             # CORS, SQLite Dialect, Security, DataSeeder
│   │   ├── security/           # JWT util + filtro
│   │   ├── validation/         # Validaciones custom (@NotFuture)
│   │   ├── model/              # Entidades JPA (13 entidades)
│   │   ├── repository/         # Repositorios Spring Data
│   │   ├── dto/                # Objetos de transferencia
│   │   ├── service/            # Lógica de negocio (14 servicios)
│   │   ├── controller/         # Endpoints REST (14 controladores)
│   │   └── exception/          # Manejo global de errores
│   ├── src/main/resources/
│   │   ├── db/migration/       # Migraciones Flyway (V1-V4)
│   │   ├── static/             # Build de producción del frontend
│   │   └── usuarios            # Configuración de usuarios
│   └── pom.xml
├── frontend/                   # React SPA
│   ├── src/
│   │   ├── api/                # Cliente Axios + endpoints
│   │   ├── context/            # AuthContext
│   │   ├── components/         # Header, ProtectedRoute
│   │   ├── pages/              # Login, Mesero, Cajero, Cocinero, Admin
│   │   └── styles/             # CSS global
│   ├── package.json
│   └── vite.config.js
├── uploads/                    # Imágenes de evidencia de pagos
├── respaldos/                  # Archivos de respaldo generados
├── build.ps1                   # Script de build para Windows
└── README.md
```

## API REST

### Autenticación
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/api/auth/login` | Autenticación (username, password) → JWT + rol |
| GET | `/api/auth/me` | Validar token y obtener usuario actual |

### Mesas
| Método | Endpoint | Rol | Descripción |
|--------|----------|-----|-------------|
| GET | `/api/mesas` | Todos autenticados | Listar mesas con estado |
| POST | `/api/mesas` | ADMIN | Crear mesa |
| PUT | `/api/mesas/{id}` | ADMIN | Actualizar mesa |
| DELETE | `/api/mesas/{id}` | ADMIN | Eliminar mesa |

### Comensales
| Método | Endpoint | Rol | Descripción |
|--------|----------|-----|-------------|
| GET | `/api/comensales?mesaId=ID` | Todos autenticados | Listar comensales por mesa |
| POST | `/api/comensales` | ADMIN, MESERO | Agregar comensal |
| DELETE | `/api/comensales/{id}` | ADMIN, MESERO | Eliminar comensal |

### Productos
| Método | Endpoint | Rol | Descripción |
|--------|----------|-----|-------------|
| GET | `/api/productos?categoria=ID&incluirInactivos=true` | ADMIN, MESERO | Listar productos |
| POST | `/api/productos` | ADMIN | Crear producto |
| PUT | `/api/productos/{id}` | ADMIN | Actualizar producto |
| DELETE | `/api/productos/{id}` | ADMIN | Eliminar (soft-delete) |
| PUT | `/api/productos/{id}/restore` | ADMIN | Restaurar producto |

### Categorías (menú)
| Método | Endpoint | Rol | Descripción |
|--------|----------|-----|-------------|
| GET | `/api/categorias` | ADMIN, MESERO | Listar categorías |
| POST | `/api/categorias` | ADMIN | Crear categoría |
| PUT | `/api/categorias/{id}` | ADMIN | Actualizar categoría |
| DELETE | `/api/categorias/{id}` | ADMIN | Eliminar categoría |

### Pedidos
| Método | Endpoint | Rol | Descripción |
|--------|----------|-----|-------------|
| GET | `/api/pedidos?comensalId=ID` | Todos autenticados | Listar pedidos por comensal |
| POST | `/api/pedidos?comensalId=ID` | ADMIN, MESERO | Crear pedido |
| POST | `/api/pedidos/{id}/detalles` | ADMIN, MESERO | Agregar producto al pedido |
| DELETE | `/api/pedidos/detalles/{detalleId}` | ADMIN, MESERO | Quitar producto del pedido |
| PUT | `/api/pedidos/{id}/cerrar` | ADMIN, MESERO | Cerrar pedido (→ COMIENDO) |
| POST | `/api/pedidos/cerrar-mesa/{mesaId}` | Todos autenticados | Cerrar cuenta de mesa |
| DELETE | `/api/pedidos/{id}` | ADMIN, MESERO | Eliminar pedido |

### Cocina
| Método | Endpoint | Rol | Descripción |
|--------|----------|-----|-------------|
| GET | `/api/cocina/comandas` | COCINERO, ADMIN | Comandas pendientes (agrupadas) |
| PUT | `/api/cocina/comandas/{id}/entregar` | COCINERO, ADMIN | Entregar comanda |

### Insumos (inventario)
| Método | Endpoint | Rol | Descripción |
|--------|----------|-----|-------------|
| GET | `/api/insumos?categoria=ID&mes=M&anio=Y` | ADMIN | Listar insumos |
| POST | `/api/insumos` | ADMIN | Crear insumo (´@NotFuture´ en fechaIngreso) |
| PUT | `/api/insumos/{id}` | ADMIN | Actualizar insumo |
| DELETE | `/api/insumos/{id}` | ADMIN | Eliminar insumo |

### Categorías de insumo
| Método | Endpoint | Rol | Descripción |
|--------|----------|-----|-------------|
| GET | `/api/categorias-insumo` | ADMIN | Listar categorías |
| POST | `/api/categorias-insumo` | ADMIN | Crear categoría |
| DELETE | `/api/categorias-insumo/{id}` | ADMIN | Eliminar categoría |

### Pagos
| Método | Endpoint | Rol | Descripción |
|--------|----------|-----|-------------|
| GET | `/api/pagos?categoria=ID&mes=M&anio=Y` | ADMIN | Listar pagos |
| POST | `/api/pagos` | ADMIN | Crear pago (´@NotFuture´ en fecha) |
| PUT | `/api/pagos/{id}` | ADMIN | Actualizar pago |
| DELETE | `/api/pagos/{id}` | ADMIN | Eliminar pago |
| POST | `/api/pagos/upload` | ADMIN | Subir imagen de evidencia |

### Categorías de pago
| Método | Endpoint | Rol | Descripción |
|--------|----------|-----|-------------|
| GET | `/api/categorias-pago` | ADMIN | Listar categorías |
| POST | `/api/categorias-pago` | ADMIN | Crear categoría |
| DELETE | `/api/categorias-pago/{id}` | ADMIN | Eliminar categoría |

### Configuración
| Método | Endpoint | Rol | Descripción |
|--------|----------|-----|-------------|
| GET | `/api/configuracion/tiempo-tolerancia` | COCINERO, ADMIN | Tolerancia de urgencia (min) |
| PUT | `/api/configuracion/tiempo-tolerancia` | ADMIN | Actualizar tolerancia |
| GET | `/api/configuracion/alerta-intervalo` | COCINERO, ADMIN | Intervalo de alerta (min) |
| PUT | `/api/configuracion/alerta-intervalo` | ADMIN | Actualizar intervalo |

### Respaldos
| Método | Endpoint | Rol | Descripción |
|--------|----------|-----|-------------|
| GET | `/api/respaldo/exportar?incluirImagenes=true/false` | ADMIN | Exportar BD + opcional imágenes (ZIP) |
| POST | `/api/respaldo/importar` | ADMIN | Importar respaldo ZIP + reinicio automático |

### Reportes PDF

Todos los reportes retornan un PDF binario. Los tickets y comandas están disponibles para cualquier autenticado; el resto solo ADMIN.

| Método | Endpoint | Descripción | Parámetros |
|--------|----------|-------------|------------|
| GET | `/api/reportes/ticket/{mesaId}` | Ticket de mesa (A5) | mesaId (path) |
| GET | `/api/reportes/comanda/{mesaId}` | Comanda para cocina (A5) | mesaId (path) |
| GET | `/api/reportes/ventas` | Reporte de ventas | fechaInicio, fechaFin |
| GET | `/api/reportes/ventas-resumen` | Ventas resumen por día | fechaInicio, fechaFin |
| GET | `/api/reportes/insumos` | Reporte de insumos | categoria?, mes?, anio? |
| GET | `/api/reportes/pagos` | Reporte de pagos | categoria?, mes?, anio? |
| GET | `/api/reportes/platillos-por-hora` | Top platillos por hora (barra) | fecha, top?, horaInicio?, horaFin?, categoria? |
| GET | `/api/reportes/kpis` | Dashboard KPIs (gráfico + tabla) | fecha, categoria? |
| GET | `/api/reportes/horas-pico` | Horas pico (barra) | fechaInicio, fechaFin |
| GET | `/api/reportes/ticket-promedio` | Ticket promedio (línea + barra) | fechaInicio, fechaFin |
| GET | `/api/reportes/tendencia-mensual` | Tendencia mensual año vs año (línea) | anio |
| GET | `/api/reportes/estacionalidad` | Estacionalidad de platillos (barra apilada) | anio, top?, vista?, horaInicio?, horaFin?, categoria? |

## Validación de fechas

Todos los formularios que permiten ingresar fechas (insumos, pagos) están protegidos con la anotación custom `@NotFuture`, que garantiza que ninguna fecha ingresada sea posterior al día actual. La validación ocurre tanto en el frontend (input `type="date"` con atributo `max`) como en el backend (JSR-380 con validador custom).

## Licencia

Uso interno. Todos los derechos reservados.
