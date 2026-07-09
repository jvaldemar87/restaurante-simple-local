# Restaurante App

[![Java](https://img.shields.io/badge/Java-17-red)](https://adoptium.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7-brightgreen)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-18-blue)](https://react.dev/)
[![SQLite](https://img.shields.io/badge/SQLite-3-lightgrey)](https://www.sqlite.org/)
[![License](https://img.shields.io/badge/licencia-Interna-orange)]()

App de gestion integral para restaurantes pequeños. Funciona 100% offline con roles de Mesero, Cajero y Administrador. Backend Java Spring Boot + SQLite, frontend React. Incluye toma de pedidos, inventario, pagos y reportes PDF.

## Caracteristicas

### Mesero
- Visualizacion de mesas con estado (libre/ocupada)
- Gestion de comensales por mesa
- Toma de pedidos con carrito por comensal
- Impresion de comandas y tickets
- Cierre de cuenta

### Cajero
- Vista de solo lectura de mesas y pedidos
- Impresion de ticket
- Cierre de cuenta de mesa

### Administrador
- CRUD completo de mesas, productos y categorias
- Control de inventario (insumos)
- Gestion de pagos (sueldos, renta, servicios)
- Reportes PDF (ventas, insumos, pagos)
- Evidencia fotografica de pagos

## Stack tecnologico

| Capa          | Tecnologia                        |
|---------------|-----------------------------------|
| Backend       | Java 17 + Spring Boot 2.7         |
| Base de datos | SQLite (archivo local)            |
| Frontend      | React 18 + React Router 6 + Vite  |
| PDF           | OpenPDF                           |
| Autenticacion | JWT + Spring Security             |

## Capturas de pantalla

<!-- Agrega tus capturas en docs/screenshots/ y descomenta esta seccion -->
<!--
| Mesero | Cajero | Admin |
|--------|--------|-------|
| ![Mesero](docs/screenshots/mesero.png) | ![Cajero](docs/screenshots/cajero.png) | ![Admin](docs/screenshots/admin.png) |
-->

## Requisitos

- Java 17+ (JDK)
- Maven 3.8+
- Node.js 18+ (solo para desarrollo)

## Instalacion y ejecucion

### Produccion (JAR unico)

```bash
cd backend
mvn clean package -DskipTests
java -jar target/restaurante-backend-1.0.0.jar
```

La aplicacion estara disponible en **http://localhost:8080**

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

Los usuarios se configuran en el archivo `backend/src/main/resources/usuarios` (sin extension).
Cada linea tiene el formato `USUARIO,CONTRASENA,ROL`. Al iniciar el servidor se cargan automaticamente.

**Contenido por defecto:**

| Usuario   | Contrasena   | Rol           |
|-----------|-------------|---------------|
| `admin`   | `admin123`   | Administrador |
| `mesero`  | `mesero123`  | Mesero        |
| `cajero`  | `cajero123`  | Cajero        |

Para cambiar una contrasena, edita el archivo y reinicia el servidor.

## Estructura del proyecto

```
RESTAURANTE/
├── backend/                    # Spring Boot API REST
│   ├── src/main/java/com/restaurante/
│   │   ├── config/             # CORS, SQLite Dialect, Security
│   │   ├── security/           # JWT util + filtro
│   │   ├── model/              # Entidades JPA (10 entidades)
│   │   ├── repository/         # Repositorios Spring Data
│   │   ├── dto/                # Objetos de transferencia
│   │   ├── service/            # Logica de negocio
│   │   ├── controller/         # Endpoints REST
│   │   └── exception/          # Manejo global de errores
│   └── pom.xml
├── frontend/                   # React SPA
│   ├── src/
│   │   ├── api/                # Cliente Axios + endpoints
│   │   ├── context/            # AuthContext
│   │   ├── components/         # Header, ProtectedRoute, etc.
│   │   ├── pages/              # Login, Mesero, Cajero, Admin
│   │   └── styles/             # CSS global
│   ├── package.json
│   └── vite.config.js
├── uploads/                    # Imagenes de evidencia de pagos
├── build.ps1                   # Script de build para Windows
└── PLAN.md                     # Plan de implementacion y fases
```

## API REST

| Metodo                    | Endpoint                              | Descripcion                  |
|---------------------------|---------------------------------------|------------------------------|
| POST                      | `/api/auth/login`                     | Autenticacion                |
| GET/POST/PUT/DELETE       | `/api/mesas`                          | CRUD mesas                   |
| GET/POST/DELETE           | `/api/comensales`                     | CRUD comensales              |
| GET/POST/PUT/DELETE       | `/api/productos`                      | CRUD productos               |
| POST                      | `/api/pedidos`                        | Crear pedido                 |
| POST                      | `/api/pedidos/{id}/detalles`          | Agregar producto al pedido   |
| PUT                       | `/api/pedidos/{id}/cerrar`            | Cerrar pedido                |
| POST                      | `/api/pedidos/cerrar-mesa/{mesaId}`   | Cerrar cuenta de mesa        |
| GET                       | `/api/reportes/ticket/{mesaId}`       | Ticket PDF                   |
| GET                       | `/api/reportes/ventas`                | Reporte de ventas PDF        |

## Licencia

Uso interno. Todos los derechos reservados.
