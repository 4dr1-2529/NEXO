package com.estacionnexo.config;

import com.estacionnexo.asistencia.service.AsistenciaService;
import com.estacionnexo.asistencia.service.AsistenciaService.Marca;
import com.estacionnexo.categoria.dto.CategoriaDatos;
import com.estacionnexo.categoria.service.CategoriaService;
import com.estacionnexo.exception.ReglaIncumplida;
import com.estacionnexo.finanzas.service.CajaService;
import com.estacionnexo.producto.CatalogoCombustibles;
import com.estacionnexo.producto.dto.ProductoDatos;
import com.estacionnexo.producto.repository.ProductoRepository;
import com.estacionnexo.producto.service.ProductoService;
import com.estacionnexo.proveedor.dto.ProveedorDatos;
import com.estacionnexo.proveedor.service.ProveedorService;
import com.estacionnexo.shared.JdbcOperaciones;
import com.estacionnexo.shared.operacion.dto.LineaSolicitud;
import com.estacionnexo.shared.operacion.dto.OperacionSolicitud;
import com.estacionnexo.shared.operacion.service.OperacionService;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Profile("demostracion")
public class CargaDemostracion {
    static final int JORNADAS = 15;
    private static final String MARCA = "Estación ficticia · carga inicial";
    private static final List<String> CUENTAS = List.of("admin", "operador", "vendedor");
    private final String esquema;
    private final JdbcTemplate jdbc;
    private final JdbcOperaciones claves;
    private final PasswordEncoder encoder;
    private final RelojDemostracion reloj;
    private final CategoriaService categorias;
    private final ProductoService productos;
    private final ProductoRepository productoRepository;
    private final ProveedorService proveedores;
    private final CajaService caja;
    private final OperacionService operaciones;
    private final AsistenciaService asistencia;
    private final Environment entorno;

    CargaDemostracion(JdbcTemplate jdbc, JdbcOperaciones claves, PasswordEncoder encoder,
                      RelojDemostracion reloj, CategoriaService categorias, ProductoService productos,
                      ProductoRepository productoRepository, ProveedorService proveedores, CajaService caja,
                      OperacionService operaciones, AsistenciaService asistencia, Environment entorno,
                      @Value("${nexo.db.schema}") String esquema) {
        this.esquema = esquema;
        this.jdbc = jdbc;
        this.claves = claves;
        this.encoder = encoder;
        this.reloj = reloj;
        this.categorias = categorias;
        this.productos = productos;
        this.productoRepository = productoRepository;
        this.proveedores = proveedores;
        this.caja = caja;
        this.operaciones = operaciones;
        this.asistencia = asistencia;
        this.entorno = entorno;
    }

    @Transactional
    public void ejecutar() {
        if (!esquema.equals(jdbc.queryForObject("SELECT DATABASE()", String.class))) {
            throw new IllegalStateException("El perfil demostracion requiere su esquema exclusivo.");
        }
        jdbc.queryForObject("SELECT id_rol FROM rol WHERE codigo='ADMINISTRADOR' FOR UPDATE", Long.class);
        long demoCompras = jdbc.queryForObject("SELECT COUNT(*) FROM compra WHERE observacion=?", Long.class, MARCA);
        if (demoCompras >= JORNADAS) {
            actualizarCatalogo();
            return;
        }
        if (demoCompras > 0) {
            actualizarCatalogo();
            completarJornadas(demoCompras);
            return;
        }
        // Flyway puede haber sembrado roles, conceptos y la categoría Gases (V5).
        long ocupados = jdbc.queryForObject("""
            SELECT (SELECT COUNT(*) FROM usuario) + (SELECT COUNT(*) FROM proveedor)
                 + (SELECT COUNT(*) FROM empleado) + (SELECT COUNT(*) FROM caja_diaria)
                 + (SELECT COUNT(*) FROM compra) + (SELECT COUNT(*) FROM venta)
            """, Long.class);
        if (ocupados != 0) {
            throw new IllegalStateException("El esquema de demostración debe estar vacío para la primera carga.");
        }

        var inicio = LocalDate.now(reloj).minusDays(JORNADAS);
        reloj.en(inicio.atTime(6, 0), () -> cargar(inicio));
    }

    private void completarJornadas(long existentes) {
        int faltan = JORNADAS - (int) existentes;
        LocalDate ultima = jdbc.queryForObject(
            "SELECT MAX(DATE(fecha_hora)) FROM compra WHERE observacion=?", LocalDate.class, MARCA);
        long admin = jdbc.queryForObject(
            "SELECT id_usuario FROM usuario WHERE username='admin' AND estado='Activo'", Long.class);
        long vendedor = jdbc.queryForObject(
            "SELECT id_usuario FROM usuario WHERE username='vendedor' AND estado='Activo'", Long.class);
        var catalogo = idsCatalogoActivo();
        var distribuidores = idsProveedores();
        for (int i = 1; i <= faltan; i++) {
            LocalDate fecha = ultima.plusDays(i);
            int indice = (int) existentes + i - 1;
            jornada(fecha, admin, vendedor, catalogo, distribuidores.get(indice % distribuidores.size()), indice);
        }
    }

    private void cargar(LocalDate inicio) {
        long admin = cuenta("admin", "ADMINISTRADOR", "99000001", "Alba");
        cuenta("operador", "OPERADOR", "99000002", "Omar");
        long vendedor = cuenta("vendedor", "VENDEDOR", "99000003", "Valeria");
        var catalogo = catalogo();
        var distribuidores = List.of(
            proveedores.crear(new ProveedorDatos("20000000001", "Distribuidora Ficticia Costa",
                "Equipo comercial", null, null, null)),
            proveedores.crear(new ProveedorDatos("20000000002", "Abastecimiento Ficticio Sierra",
                "Equipo comercial", null, null, null)));

        for (int dia = 0; dia < JORNADAS; dia++) {
            LocalDate fecha = inicio.plusDays(dia);
            jornada(fecha, admin, vendedor, catalogo, distribuidores.get(dia % 2), dia);
        }
    }

    private List<Long> catalogo() {
        Map<String, Long> categoriasIds = new HashMap<>();
        categoriasIds.put(CatalogoCombustibles.CAT_GASOLINAS,
            asegurarCategoria(CatalogoCombustibles.CAT_GASOLINAS, CatalogoCombustibles.DESC_GASOLINAS));
        categoriasIds.put(CatalogoCombustibles.CAT_DIESEL,
            asegurarCategoria(CatalogoCombustibles.CAT_DIESEL, CatalogoCombustibles.DESC_DIESEL));
        categoriasIds.put(CatalogoCombustibles.CAT_GASES,
            asegurarCategoria(CatalogoCombustibles.CAT_GASES, CatalogoCombustibles.DESC_GASES));

        List<Long> ids = new ArrayList<>();
        for (var item : CatalogoCombustibles.ACTIVOS) {
            var existente = productoRepository.buscarPorCodigo(item.codigo(), true);
            if (existente.isPresent()) {
                productoRepository.sincronizarMetadatos(item.codigo(), categoriasIds.get(item.categoria()),
                    item.nombre(), item.unidad());
                ids.add(existente.get().id());
            } else {
                ids.add(productos.crear(new ProductoDatos(item.codigo(), categoriasIds.get(item.categoria()),
                    item.nombre(), precioDemo(item.codigo()), item.unidad())));
            }
        }
        return ids;
    }

    private List<Long> idsCatalogoActivo() {
        return jdbc.query("""
            SELECT p.id_producto FROM producto p
            JOIN categoria c ON c.id_categoria=p.id_categoria
            WHERE p.estado='Activo' AND c.estado='Activo'
              AND p.codigo_producto IN ('GAS-REG','GAS-PRE','DIE-B5S50','GLP-AUTO','GNV-C')
            ORDER BY FIELD(p.codigo_producto,'GAS-REG','GAS-PRE','DIE-B5S50','GLP-AUTO','GNV-C')
            """, (rs, i) -> rs.getLong(1));
    }

    private List<Long> idsProveedores() {
        var ids = jdbc.query("SELECT id_proveedor FROM proveedor WHERE estado='Activo' ORDER BY id_proveedor",
            (rs, i) -> rs.getLong(1));
        if (ids.isEmpty()) {
            throw new IllegalStateException("No hay proveedores activos para completar la demostración.");
        }
        return ids;
    }

    private long asegurarCategoria(String nombre, String descripcion) {
        Long id = jdbc.query("""
            SELECT id_categoria FROM categoria WHERE nombre=?
            """, (rs, i) -> rs.getLong(1), nombre).stream().findFirst().orElse(null);
        if (id != null) {
            jdbc.update("UPDATE categoria SET descripcion=?, estado='Activo' WHERE id_categoria=?",
                descripcion, id);
            return id;
        }
        return categorias.crear(new CategoriaDatos(nombre, descripcion));
    }

    private void actualizarCatalogo() {
        jdbc.update("UPDATE categoria SET descripcion=? WHERE nombre=?",
            CatalogoCombustibles.DESC_GASOLINAS, CatalogoCombustibles.CAT_GASOLINAS);
        jdbc.update("UPDATE categoria SET descripcion=? WHERE nombre=?",
            CatalogoCombustibles.DESC_DIESEL, CatalogoCombustibles.CAT_DIESEL);
        if (jdbc.queryForObject("SELECT COUNT(*) FROM categoria WHERE nombre=?", Long.class,
            CatalogoCombustibles.CAT_GASES) == 0) {
            categorias.crear(new CategoriaDatos(CatalogoCombustibles.CAT_GASES, CatalogoCombustibles.DESC_GASES));
        } else {
            jdbc.update("UPDATE categoria SET descripcion=?, estado='Activo' WHERE nombre=?",
                CatalogoCombustibles.DESC_GASES, CatalogoCombustibles.CAT_GASES);
        }

        var legacy = Map.of("DEMO-GR", "GAS-REG", "DEMO-GP", "GAS-PRE", "DEMO-DB5", "DIE-B5S50");
        legacy.forEach((anterior, nuevo) -> {
            if (jdbc.queryForObject("SELECT COUNT(*) FROM producto WHERE codigo_producto=?", Long.class, nuevo) == 0) {
                jdbc.update("UPDATE producto SET codigo_producto=? WHERE codigo_producto=?", nuevo, anterior);
            }
        });

        long gasesId = jdbc.queryForObject("SELECT id_categoria FROM categoria WHERE nombre=?",
            Long.class, CatalogoCombustibles.CAT_GASES);
        for (var item : CatalogoCombustibles.ACTIVOS) {
            if (productoRepository.buscarPorCodigo(item.codigo(), true).isPresent()) {
                long catId = item.categoria().equals(CatalogoCombustibles.CAT_GASES) ? gasesId
                    : jdbc.queryForObject("SELECT id_categoria FROM categoria WHERE nombre=?", Long.class, item.categoria());
                productoRepository.sincronizarMetadatos(item.codigo(), catId, item.nombre(), item.unidad());
                continue;
            }
            long catId = jdbc.queryForObject("SELECT id_categoria FROM categoria WHERE nombre=?",
                Long.class, item.categoria());
            productos.crear(new ProductoDatos(item.codigo(), catId, item.nombre(),
                precioDemo(item.codigo()), item.unidad()));
        }
    }

    private BigDecimal precioDemo(String codigo) {
        String configurado = entorno.getProperty("nexo.catalogo.precio." + codigo);
        if (configurado != null && !configurado.isBlank()) {
            return new BigDecimal(configurado.strip());
        }
        return switch (codigo) {
            case "GAS-REG" -> new BigDecimal("5.10");
            case "GAS-PRE" -> new BigDecimal("5.85");
            case "DIE-B5S50" -> new BigDecimal("4.70");
            case "GLP-AUTO" -> new BigDecimal("8.90");
            case "GNV-C" -> new BigDecimal("1.85");
            default -> throw new IllegalStateException("Sin precio de demostración para " + codigo);
        };
    }

    private long cuenta(String usuario, String rol, String dni, String nombre) {
        LocalDateTime fecha = LocalDateTime.now(reloj);
        long empleado = claves.insertar("""
            INSERT INTO empleado (dni,nombres,apellidos,cargo,telefono,fecha_ingreso,estado)
            VALUES (?,?,?,?,?,?,'Activo')
            """, dni, nombre, "Nexo", rol, "000000000", fecha.toLocalDate());
        return claves.insertar("""
            INSERT INTO usuario (id_empleado,id_rol,username,password_hash,estado,fecha_creacion)
            SELECT ?,id_rol,?,?,'Activo',? FROM rol WHERE codigo=? AND estado='Activo'
            """, empleado, usuario, encoder.encode("nexo2026"), fecha, rol);
    }

    private void jornada(LocalDate dia, long admin, long vendedor, List<Long> catalogo, long proveedor, int indice) {
        var entradas = new ArrayList<LineaSolicitud>();
        var salidas = new ArrayList<LineaSolicitud>();
        for (int i = 0; i < catalogo.size(); i++) {
            // Varianza determinista (sin Random): solo afecta cargas nuevas / jornadas faltantes.
            long ondaCompra = Math.round(7 * Math.sin((indice + 1) * 0.85 + i * 0.55));
            long ondaVenta = Math.round(5 * Math.cos((indice + 2) * 1.05 + i * 0.4));
            entradas.add(new LineaSolicitud(catalogo.get(i),
                BigDecimal.valueOf(Math.max(40L, 90L + i * 8 + indice * 3 + ondaCompra)),
                new BigDecimal("3.50").add(BigDecimal.valueOf(i, 1))));
            salidas.add(new LineaSolicitud(catalogo.get(i),
                BigDecimal.valueOf(Math.max(15L, 42L + i * 5 + indice * 2 + ondaVenta)), null));
        }
        int desfase = indice % 5;
        reloj.en(dia.atTime(7, 25 + desfase), () -> CUENTAS.forEach(u -> marcarSiFalta(u, Marca.ENTRADA)));
        reloj.en(dia.atTime(8, desfase), () -> abrirCajaSiFalta(admin));
        reloj.en(dia.atTime(9, 10 + desfase), () -> operaciones.comprar(admin,
            new OperacionSolicitud(token(dia, "compra"), proveedor, MARCA, entradas)));
        reloj.en(dia.atTime(15, 40 + desfase), () -> operaciones.vender(vendedor,
            new OperacionSolicitud(token(dia, "venta"), null, null, salidas)));
        reloj.en(dia.atTime(18, desfase), () -> cerrarCajaSiAbierta(admin));
        reloj.en(dia.atTime(18, 10 + desfase), () -> CUENTAS.forEach(u -> marcarSiFalta(u, Marca.SALIDA)));
    }

    private void marcarSiFalta(String usuario, Marca marca) {
        long empleado = jdbc.queryForObject("""
            SELECT u.id_empleado FROM usuario u WHERE u.username=? AND u.estado='Activo'
            """, Long.class, usuario);
        LocalDate fecha = LocalDate.now(reloj);
        if (marca == Marca.ENTRADA) {
            if (jdbc.queryForObject("SELECT COUNT(*) FROM asistencia WHERE id_empleado=? AND fecha=?",
                    Long.class, empleado, fecha) == 0) {
                asistencia.marcar(usuario, marca);
            }
            return;
        }
        if (jdbc.queryForObject("""
                SELECT COUNT(*) FROM asistencia WHERE id_empleado=? AND fecha=? AND hora_salida IS NULL
                """, Long.class, empleado, fecha) > 0) {
            asistencia.marcar(usuario, marca);
        }
    }

    private void abrirCajaSiFalta(long admin) {
        var actual = caja.actual();
        if (actual.isEmpty()) {
            caja.abrir(admin, new BigDecimal("3500.00"));
            return;
        }
        if (!actual.get().abierta()) {
            throw new ReglaIncumplida("Ya existe una caja cerrada para " + actual.get().fecha());
        }
    }

    private void cerrarCajaSiAbierta(long admin) {
        if (caja.actual().filter(c -> c.abierta()).isPresent()) {
            caja.cerrar(admin);
        }
    }

    private UUID token(LocalDate dia, String tipo) {
        return UUID.nameUUIDFromBytes(("estacion-nexo-demostracion:" + dia + ":" + tipo)
            .getBytes(StandardCharsets.UTF_8));
    }
}
