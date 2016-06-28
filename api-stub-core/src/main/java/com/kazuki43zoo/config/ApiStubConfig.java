package com.kazuki43zoo.config;

import org.h2.server.web.DbStarter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
public class ApiStubConfig extends WebMvcConfigurerAdapter {

    @Bean
    DbStarter dbStarter() {
        return new DbStarter();
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addRedirectViewController("/", "/manager/mocks");
    }

}
