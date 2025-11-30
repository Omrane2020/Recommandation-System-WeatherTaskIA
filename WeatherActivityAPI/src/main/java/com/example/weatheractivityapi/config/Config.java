package com.example.weatheractivityapi.config;

import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Config extends ResourceConfig {
    public Config() {
        packages("com.example.weatheractivityapi.web"); 
    }
}
