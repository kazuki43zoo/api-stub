package com.kazuki43zoo.config;

import org.h2.server.web.DbStarter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApiStubConfig {

    @Bean
    DbStarter dbStarter() {
        return new DbStarter();
    }

}
