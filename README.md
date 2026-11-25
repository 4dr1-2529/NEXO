# Estación NEXO

Sistema académico de estación de servicio (Perú): Spring Boot, JDBC, Security, Thymeleaf, Flyway y MySQL vía XAMPP. **Sin JavaScript.**

## Tecnologías

Java · Maven · Spring Boot · Spring JDBC · Spring Security · Thymeleaf · Flyway · MySQL Connector · HTML/CSS/SVG

## Requisitos

- Java JDK 26 o 27
- Apache Maven 3.9+
- XAMPP con servicio **MySQL** activo (MariaDB compatible incluido; **no** instalar MariaDB/MySQL Server aparte)
- IntelliJ IDEA opcional

## Arranque rápido

1. En XAMPP: **Start** junto a MySQL (puerto 3306; `root` sin contraseña en la instalación típica).
2. Desde la raíz del proyecto:

```text
mvn clean install
mvn -pl estacion-nexo-intellij spring-boot:run
```

3. Abrir la dirección oficial **http://localhost:8080/login** (puerto 8080; `/` redirige a `/login`).

Cuentas de demostración: `admin`, `operador`, `vendedor` / `nexo2026`.

En el primer arranque (perfil `demostracion`) se crea el esquema si falta, se aplican Flyway **V1–V7**, el catálogo, los usuarios y quince jornadas. Los reinicios no duplican datos.

Con XAMPP por defecto **no** hace falta `nexo-local.properties`. Si personalizó puerto o clave, copie `estacion-nexo-intellij/config/nexo-local.properties.example` a `nexo-local.properties` (no se versiona).

## IntelliJ

1. Abrir la carpeta raíz como proyecto Maven (Project SDK).
2. Ejecutar la configuración compartida **Estación NEXO** (botón verde Run).

## Base de datos

Una sola base operativa (por defecto `estacion_nexo_demostracion`). Las migraciones V1–V7 son historial de estructura, no bases distintas.

## Funcionalidades

Login por roles · catálogo de combustibles · compras/ventas · inventario · caja · dashboards · reportes (solo administrador) · asistencia

[Detalle de instalación y catálogo](estacion-nexo-intellij/README.md)
