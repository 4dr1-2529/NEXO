package com.estacionnexo.config;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;

@Configuration
@Profile("demostracion")
class DemostracionConfig {
    @Bean
    @Primary
    RelojDemostracion relojDemostracion() {
        return new RelojDemostracion();
    }

    @Bean
    @Order(1)
    ApplicationRunner cargarDemostracion(CargaDemostracion carga) {
        return args -> carga.ejecutar();
    }
}
