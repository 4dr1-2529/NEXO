package com.estacionnexo;

import com.estacionnexo.auth.service.CuentaService;
import com.estacionnexo.categoria.dto.CategoriaDatos;
import com.estacionnexo.categoria.service.CategoriaService;
import com.estacionnexo.exception.ReglaIncumplida;
import com.estacionnexo.finanzas.service.CajaService;
import com.estacionnexo.producto.dto.ProductoDatos;
import com.estacionnexo.producto.service.ProductoService;
import com.estacionnexo.proveedor.dto.ProveedorDatos;
import com.estacionnexo.proveedor.service.ProveedorService;
import com.estacionnexo.shared.operacion.dto.LineaSolicitud;
import com.estacionnexo.shared.operacion.dto.OperacionSolicitud;
import com.estacionnexo.shared.operacion.service.OperacionService;
import java.math.BigDecimal;
import java.sql.DriverManager;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.SpringApplication;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@EnabledIfEnvironmentVariable(named = "NEXO_TEST_XAMPP", matches = "true")
class XamppOperacionesTest {
    @Test
    void migraConciliaConcurrenciaYConservaDatosAlReiniciar() throws Exception {
        String esquema = "estacion_nexo_test_" + UUID.randomUUID().toString().replace("-", "");
        String puerto = variable("NEXO_TEST_PORT", "3307");
        if (!List.of("3306", "3307").contains(puerto)) {
            throw new IllegalArgumentException("Puerto de prueba de XAMPP inválido.");
        }
        String usuarioSql = variable("NEXO_TEST_USER", "root");
        String claveSql = variable("NEXO_TEST_PASSWORD", "");
        String url = "jdbc:mysql://127.0.0.1:" + puerto + "/";
        var argumentos = new String[]{
            "--spring.profiles.active=local", "--server.port=0", "--spring.datasource.url=" + url + esquema + "?createDatabaseIfNotExist=true",
            "--spring.datasource.username=" + usuarioSql, "--spring.datasource.password=" + claveSql,
            "--nexo.admin.username=prueba_integracion", "--nexo.admin.password=" + UUID.randomUUID(),
            "--nexo.admin.dni=00000001", "--nexo.admin.nombres=Prueba", "--nexo.admin.apellidos=Integración",
            "--nexo.admin.telefono=000000000"
        };
        long producto;
        try {
            try (var app = SpringApplication.run(NexoApplication.class, argumentos)) {
                var cuentas = app.getBean(CuentaService.class);
                var responsable = cuentas.buscar("prueba_integracion").orElseThrow().usuario();
                var categorias = app.getBean(CategoriaService.class);
                var productos = app.getBean(ProductoService.class);
                var proveedores = app.getBean(ProveedorService.class);
                var caja = app.getBean(CajaService.class);
                var operaciones = app.getBean(OperacionService.class);
                var jdbc = app.getBean(JdbcTemplate.class);

                long categoria = categorias.crear(new CategoriaDatos("Prueba", null));
                producto = productos.crear(new ProductoDatos("P001", categoria, "Combustible", decimal("8")));
                long proveedor = proveedores.crear(new ProveedorDatos("10000000001", "Proveedor", null,
                    null, null, null));
                caja.abrir(responsable, decimal("1000"));
                var compra = new OperacionSolicitud(UUID.randomUUID(), proveedor, null,
                    List.of(new LineaSolicitud(producto, decimal("100"), decimal("5"))));
                operaciones.comprar(responsable, compra);
                assertThrows(DuplicateKeyException.class, () -> operaciones.comprar(responsable, compra));

                categorias.cambiarEstado(categoria, false);
                assertThrows(ReglaIncumplida.class,
                    () -> operaciones.vender(responsable, venta(producto, "60")));
                categorias.cambiarEstado(categoria, true);

                var inicio = new CountDownLatch(1);
                Callable<Integer> vender = () -> {
                    inicio.await();
                    try {
                        operaciones.vender(responsable, venta(producto, "60"));
                        return 1;
                    } catch (ReglaIncumplida e) {
                        return 0;
                    }
                };
                try (var executor = Executors.newFixedThreadPool(2)) {
                    var primero = executor.submit(vender);
                    var segundo = executor.submit(vender);
                    inicio.countDown();
                    assertEquals(1, primero.get(15, TimeUnit.SECONDS) + segundo.get(15, TimeUnit.SECONDS));
                }
                assertEquals(decimal("40.000"), productos.buscar(producto, false).existencias());
                assertEquals(2, jdbc.queryForObject("SELECT COUNT(*) FROM movimiento_inventario", Integer.class));
                assertEquals(2, jdbc.queryForObject("SELECT COUNT(*) FROM movimiento_caja", Integer.class));
                assertEquals(decimal("980.00"), caja.cerrar(responsable));
                assertThrows(ReglaIncumplida.class,
                    () -> operaciones.vender(responsable, venta(producto, "1")));
            }
            try (var reinicio = SpringApplication.run(NexoApplication.class, argumentos)) {
                assertEquals(decimal("40.000"), reinicio.getBean(ProductoService.class).buscar(producto, false).existencias());
                var jdbc = reinicio.getBean(JdbcTemplate.class);
                assertEquals(decimal("980.00"), jdbc.queryForObject(
                    "SELECT saldo_cierre FROM caja_diaria", BigDecimal.class));
                assertEquals(7, jdbc.queryForObject(
                    "SELECT COUNT(*) FROM flyway_schema_history WHERE success AND version IS NOT NULL", Integer.class));
            }
        } finally {
            try (var conexion = DriverManager.getConnection(url, usuarioSql, claveSql);
                 var sentencia = conexion.createStatement()) {
                // El identificador procede exclusivamente de un UUID generado en esta prueba.
                sentencia.executeUpdate("DROP DATABASE IF EXISTS " + esquema);
            }
        }
    }

    private static OperacionSolicitud venta(long producto, String litros) {
        return new OperacionSolicitud(UUID.randomUUID(), null, null,
            List.of(new LineaSolicitud(producto, decimal(litros), null)));
    }

    private static BigDecimal decimal(String valor) {
        return new BigDecimal(valor);
    }

    private static String variable(String nombre, String alternativa) {
        return System.getenv().getOrDefault(nombre, alternativa);
    }
}
