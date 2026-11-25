package com.estacionnexo.security;

import com.estacionnexo.auditoria.AuditoriaContexto;
import com.estacionnexo.auth.service.CuentaService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.web.filter.OncePerRequestFilter;

@Configuration
public class SecurityConfig {
    private static final String ADMIN = "ADMINISTRADOR";
    private static final String OPERADOR = "OPERADOR";
    private static final String VENDEDOR = "VENDEDOR";

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    @Profile("!demostracion")
    ApplicationRunner primerAdministrador(CuentaService cuentas, Environment entorno) {
        return args -> {
            String username = entorno.getProperty("nexo.admin.username", "");
            if (!username.isBlank()) {
                cuentas.inicializar(username, entorno.getRequiredProperty("nexo.admin.password"),
                    entorno.getRequiredProperty("nexo.admin.dni"), entorno.getRequiredProperty("nexo.admin.nombres"),
                    entorno.getRequiredProperty("nexo.admin.apellidos"), entorno.getRequiredProperty("nexo.admin.telefono"));
            }
        };
    }

    @Bean
    SecurityFilterChain web(HttpSecurity http, CuentaService cuentas, AuditoriaContexto auditoria) throws Exception {
        var ingreso = new SavedRequestAwareAuthenticationSuccessHandler();
        ingreso.setDefaultTargetUrl("/dashboard");
        return http.authorizeHttpRequests(permisos -> permisos
                .requestMatchers("/", "/contacto", "/login", "/css/**", "/error").permitAll()
                .requestMatchers("/reportes", "/reportes/**").hasRole(ADMIN)
                .requestMatchers("/proveedores/**", "/compras/**", "/finanzas/**", "/empleados/**",
                    "/usuarios/**", "/asistencia/personal/**", "/api/v1/proveedores/**",
                    "/api/v1/compras/**", "/api/v1/caja/**", "/api/v1/asistencia/personal/**").hasRole(ADMIN)
                .requestMatchers("/ventas/**", "/api/v1/ventas/**").hasAnyRole(ADMIN, VENDEDOR)
                .requestMatchers("/inventario/entrada", "/inventario/salida", "/inventario/movimientos",
                    "/api/v1/inventario/movimientos", "/api/v1/inventario/ajustes").hasAnyRole(ADMIN, OPERADOR)
                .requestMatchers("/categorias/nueva", "/categorias/*/editar", "/combustibles/nuevo",
                    "/combustibles/*/editar").hasRole(ADMIN)
                .requestMatchers(HttpMethod.GET, "/categorias/**", "/combustibles/**",
                    "/api/v1/categorias/**", "/api/v1/combustibles/**").authenticated()
                .requestMatchers("/categorias/**", "/combustibles/**",
                    "/api/v1/categorias/**", "/api/v1/combustibles/**").hasRole(ADMIN)
                .anyRequest().authenticated())
            .formLogin(form -> form.loginPage("/login")
                .successHandler((request, response, auth) -> {
                    cuentas.registrarAcceso(auth.getName());
                    auditoria.login(auth.getName(), true, "Sesión iniciada");
                    ingreso.onAuthenticationSuccess(request, response, auth);
                })
                .failureHandler((request, response, exception) -> {
                    String usuario = request.getParameter("username");
                    auditoria.login(usuario, false, "Credenciales inválidas");
                    response.sendRedirect(request.getContextPath() + "/login?error");
                }))
            .logout(logout -> logout.logoutSuccessUrl("/login?salida"))
            .addFilterBefore(new OncePerRequestFilter() {
                @Override
                protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                                FilterChain chain) throws ServletException, IOException {
                    var auth = SecurityContextHolder.getContext().getAuthentication();
                    if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
                        var cuenta = cuentas.buscar(auth.getName());
                        if (cuenta.isEmpty() || !cuenta.get().activa() || auth.getAuthorities().stream()
                                .noneMatch(rol -> rol.getAuthority().equals("ROLE_" + cuenta.get().rol()))) {
                            var session = request.getSession(false);
                            if (session != null) {
                                session.invalidate();
                            }
                            SecurityContextHolder.clearContext();
                        }
                    }
                    chain.doFilter(request, response);
                }
            }, AuthorizationFilter.class)
            .build();
    }
}
