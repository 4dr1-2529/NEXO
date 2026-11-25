package com.estacionnexo.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.LinkedHashSet;
import java.util.Set;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.util.WebUtils;

@Configuration
class EnvioWebConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new Envios()).excludePathPatterns("/api/**", "/login", "/logout", "/error");
    }

    private static final class Envios implements HandlerInterceptor {
        private static final String ATRIBUTO = Envios.class.getName();
        private static final int MAXIMO = 256;

        @Override
        @SuppressWarnings("unchecked")
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
                throws Exception {
            String token = request.getParameter("_envio");
            if (!"POST".equals(request.getMethod()) || token == null) {
                return true;
            }
            if (!token.matches("[0-9a-f-]{36}")) {
                response.sendError(400);
                return false;
            }
            var sesion = request.getSession();
            String clave = request.getServletPath() + ":" + token;
            synchronized (WebUtils.getSessionMutex(sesion)) {
                var usados = (Set<String>) sesion.getAttribute(ATRIBUTO);
                if (usados == null) {
                    usados = new LinkedHashSet<>();
                    sesion.setAttribute(ATRIBUTO, usados);
                }
                if (!usados.add(clave)) {
                    response.sendError(409);
                    return false;
                }
                if (usados.size() > MAXIMO) {
                    var primero = usados.iterator();
                    primero.next();
                    primero.remove();
                }
            }
            return true;
        }
    }
}
