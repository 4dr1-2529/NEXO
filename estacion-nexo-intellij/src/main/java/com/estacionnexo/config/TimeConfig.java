package com.estacionnexo.config;

import java.time.Clock;
import java.time.ZoneId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TimeConfig {
    public static final ZoneId ZONA_NEXO = ZoneId.of("America/Lima");

    @Bean
    Clock serverClock() {
        return Clock.system(ZONA_NEXO);
    }
}
