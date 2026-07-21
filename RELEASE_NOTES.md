# 🍽️ Restaurante App v1.0.0 — Primer Lanzamiento Oficial

¡Bienvenido al primer release estable de **Restaurante App**! Un sistema de gestión integral para restaurantes, 100% open source y completamente dockerizado. No necesitas instalar Java, Node.js ni configurar bases de datos: solo Docker.

---

## 🎉 Novedades

### Gestión integral con 4 roles

| Rol | Funcionalidades |
|---|---|
| 🧑‍🍳 **Mesero** | Visualizar mesas, gestionar comensales, tomar pedidos, imprimir tickets y cerrar cuentas |
| 💰 **Cajero** | Consultar mesas y pedidos, imprimir ticket de cuenta, cerrar cuentas |
| 👨‍🍳 **Cocinero** | Vista de cocina en tiempo real con comandas estilo post-it, marcar como entregado, alerta de urgencia |
| ⚙️ **Admin** | CRUD de mesas, menú, insumos y pagos. Dashboard de ventas, 12 reportes PDF con gráficos y respaldos |

### Vista de cocina en tiempo real

Las comandas aparecen como tarjetas post-it ordenadas por urgencia. Incluye:
- Agrupación inteligente de productos idénticos
- Alerta sonora (beep triple) al recibir nuevas comandas
- Indicador visual de urgencia cuando el tiempo de tolerancia se excede (configurable desde Admin)
- Botón para marcar comandas como entregadas

### 12 reportes PDF con gráficos

Genera reportes profesionales en PDF con gráficos de barras y líneas (JFreeChart):
- Ventas por día, semana y mes
- Consumo de insumos
- Gastos y pagos
- KPIs, tendencias, estacionalidad y horas pico

### Funcionamiento offline

Todo se ejecuta de forma local con **SQLite** como base de datos embebida. No requiere conexión a internet, servidores externos ni servicios en la nube.

### Build multi-etapa con Docker

El `Dockerfile` incluye 3 etapas optimizadas:
1. **Frontend (Node 18 Alpine):** Compila React + Vite a estáticos
2. **Backend (Maven 3.9 + JDK 17 Alpine):** Empaqueta Spring Boot con los estáticos
3. **Producción (JRE 17 Alpine):** Imagen final ligera ejecutada como usuario no-root

### Autenticación con JWT

Seguridad basada en JSON Web Tokens con expiración de 12 horas y protección de rutas por rol en frontend y backend.

---

## 🚀 Guía de Inicio Rápido

### Requisitos previos

- **Docker** y **Docker Compose** instalados en tu máquina

### Paso 1: Descargar el código fuente

Descarga el archivo `Source code (zip)` o `Source code (tar.gz)` desde la sección de Assets de este release y descomprímelo.

### Paso 2: Levantar la aplicación

```bash
cd restaurante-simple-local
docker compose up -d --build
```

La primera ejecución tomará unos minutos mientras Docker descarga las imágenes base y compila el proyecto. Las ejecuciones posteriores serán instantáneas.

### Paso 3: Acceder al sistema

Abre tu navegador en:

```
http://localhost:8080
```

Usa cualquiera de las credenciales de la sección [🔐 Credenciales por defecto](#-credenciales-por-defecto) para iniciar sesión.

### Paso 4: Detener la aplicación

```bash
docker compose down
```

---

## ⚙️ Configuración de Red (CORS)

Por defecto, el sistema acepta peticiones desde cualquier origen (`CORS_ORIGINS=*`). Esto es ideal para desarrollo local, pero **si deseas exponer el servicio en una red local o un dominio público**, debes restringir los orígenes permitidos editando el archivo `docker-compose.yml`:

```yaml
services:
  restaurante-app:
    # ...
    environment:
      - CORS_ORIGINS=http://192.168.1.50:8080,http://mi-dominio.com
```

**Ejemplos de configuración:**

| Escenario | Valor de `CORS_ORIGINS` |
|---|---|
| Desarrollo local (por defecto) | `*` |
| Red local con IP fija | `http://192.168.1.50:8080` |
| Dominio propio | `https://restaurante.mi-negocio.com` |
| Múltiples orígenes | `http://localhost:3000,http://192.168.1.50:8080` |

> **Importante:** También debes cambiar `JWT_SECRET` por una clave secreta robusta antes de exponer el servicio fuera de tu red local de confianza.

Luego de modificar el archivo, reconstruye y reinicia:

```bash
docker compose up -d --build
```

---

## 🔐 Credenciales por defecto

Al iniciar la aplicación por primera vez, se crean automáticamente los siguientes usuarios:

| Usuario | Contraseña | Rol | Permisos |
|---|---|---|---|
| `admin` | `admin123` | ADMIN | Acceso total al sistema |
| `mesero` | `mesero123` | MESERO | Gestión de mesas, comensales y pedidos |
| `cajero` | `cajero123` | CAJERO | Consulta de mesas, impresión y cierre de cuentas |
| `cocinero` | `cocinero123` | COCINERO | Vista de comandas en cocina |

> ⚠️ **Recomendación de seguridad:** Cambia todas las contraseñas desde el panel de administración tras el primer inicio de sesión.

---

## 🛠️ Stack Tecnológico

| Capa | Tecnología |
|---|---|
| Backend | Java 17 + Spring Boot 2.7 |
| Base de datos | SQLite (archivo local) |
| Frontend | React 18 + React Router 6 + Vite |
| PDF | OpenPDF + JFreeChart |
| Autenticación | JWT + Spring Security |
| Contenedores | Docker + Docker Compose (build multi-etapa) |

---

## 📂 Estructura del proyecto

```
restaurante-simple-local/
├── backend/           # API REST Spring Boot
├── frontend/          # SPA React + Vite
├── docker-compose.yml # Orquestación de contenedores
├── Dockerfile         # Build multi-etapa
└── docker-data/       # Datos persistentes (generado en tiempo de ejecución)
    ├── db/            # Base de datos SQLite
    ├── uploads/       # Imágenes subidas
    └── respaldos/     # Respaldos del sistema
```

---

**Happy cooking! 🍳**
