# Estación Nexo

Sistema académico de una estación de servicio peruana. La web se genera con Spring MVC y Thymeleaf; menú, formularios y gráficos funcionan con HTML, CSS y SVG. **No utiliza JavaScript.**

## Requisitos

- Java JDK 26 o 27.
- Apache Maven.
- XAMPP con el servicio **MySQL** activo. XAMPP incluye MariaDB como motor compatible; los logs pueden decir “MariaDB”. **No instale MariaDB ni otro servidor de BD por separado.**

IntelliJ IDEA es opcional.

## Abrir el proyecto

1. (Opcional) Abre la carpeta raíz `GASOLINERIA` en IntelliJ como proyecto Maven con el **Project SDK**.
2. En XAMPP, **Start** junto a **MySQL** (puerto 3306, `root` sin contraseña en la instalación típica). Con esa configuración predeterminada **no** hace falta `nexo-local.properties`. Solo si personalizó XAMPP, copie `config/nexo-local.properties.example` a `config/nexo-local.properties` (no se versiona).
3. Arranque: configuración **Estación NEXO**, **Maven Spring Boot Run**, o los comandos de abajo. Dirección oficial: **http://localhost:8080/login** (puerto 8080; `/` redirige a `/login`).
4. Entra con `admin`, `operador` o `vendedor` y la contraseña `nexo2026` (perfil `demostracion`).

En el primer arranque, el perfil `demostracion` crea el esquema si no existe, ejecuta Flyway **V1–V7**, asegura el catálogo (3 categorías / 5 productos), crea los tres usuarios con BCrypt y genera **quince** jornadas coherentes. La carga es idempotente: reiniciar no duplica compras, ventas ni usuarios.

Flyway aplica **V1–V7** sobre un único esquema operativo (por defecto `estacion_nexo_demostracion`): una estructura final, no siete bases.

Detén la aplicación con **Stop** antes de cambiar de Run, para liberar el puerto 8080.

### Comandos Maven

Desde la raíz `GASOLINERIA`:

```text
mvn clean install
mvn -pl estacion-nexo-intellij spring-boot:run
```

Desde este módulo:

```text
mvn clean install
mvn spring-boot:run
```

El POM raíz no ejecuta el fat jar (`spring-boot-maven-plugin` con `skip=true`); el módulo `estacion-nexo-intellij` sí.

## Catálogo peruano (activo)

Tres categorías y cinco productos:

| Categoría | Código | Producto | Unidad |
|-----------|--------|----------|--------|
| Gasolinas | `GAS-REG` | Gasolina Regular | L |
| Gasolinas | `GAS-PRE` | Gasolina Premium | L |
| Diésel | `DIE-B5S50` | Diésel B5 S-50 | L |
| Gases vehiculares | `GLP-AUTO` | GLP para uso automotor | gal |
| Gases vehiculares | `GNV-C` | Gas Natural Vehicular Comprimido | m3 |

Los gasoholes no forman parte del catálogo activo (quedan archivados si existían). Los precios y existencias de demostración solo se cargan con el perfil `demostracion`; las migraciones Flyway no insertan precios ni stock ficticios en bases reales.

Referencias: [Gasolina Regular](https://www.petroperu.com.pe/Docs/spa/files/productos/especificaciones-tecnicas-gasolina-superplus-regular.pdf) · [Gasolina Premium](https://www.petroperu.com.pe/Docs/eng/files/productos/et-gas-premium.pdf) · [GLP y GNV en Perú](https://www.gob.pe/institucion/inacal/noticias/1143361-inacal-aprueba-nuevas-normas-tecnicas-peruanas-para-fortalecer-la-seguridad-y-eficiencia-en-el-transporte-automotor)

## Respaldo y restauración

Exporta el esquema con `mysqldump` de XAMPP (`--single-transaction`), incluyendo el historial de Flyway. Restaura en un esquema vacío y verifica existencias, caja y registros antes de cambiar de instalación.

## Pruebas y API

```text
mvn test
```

Con integración XAMPP (esquema temporal aislado):

```text
set NEXO_TEST_XAMPP=true
mvn test
```

Opcional: `NEXO_TEST_PORT`, `NEXO_TEST_USER`, `NEXO_TEST_PASSWORD`.

Colección Postman: [Estación Nexo](postman/Estacion-Nexo.postman_collection.json) (sesión y CSRF).
