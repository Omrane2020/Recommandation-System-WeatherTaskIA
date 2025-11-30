package com.example.weatheractivityapi.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import com.example.weatheractivityapi.model.Activity;
import com.example.weatheractivityapi.repository.ActivityRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.List;

@Component
public class DataLoader implements CommandLineRunner {

    @Autowired
    private ActivityRepository repo;

    @Override
    public void run(String... args) throws Exception {
        if (repo.count() == 0) {
            ObjectMapper mapper = new ObjectMapper();
            
            try (InputStream inputStream = new ClassPathResource("static/activities.json").getInputStream()) {
                List<Activity> activities = mapper.readValue(
                    inputStream,
                    new TypeReference<List<Activity>>() {}
                );
                
                repo.saveAll(activities);
                System.out.println(activities.size() + " activities loaded successfully!");
            }
        } else {
            System.out.println("Data already loaded, skipping initialization.");
        }
    }
}