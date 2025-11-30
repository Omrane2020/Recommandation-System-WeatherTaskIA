package com.example.weatheractivityapi.service;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.weatheractivityapi.model.Activity;
import com.example.weatheractivityapi.repository.ActivityRepository;

@Service
public class ActivityService {

    @Autowired
    private ActivityRepository repo;

    public List<Activity> getAll() {
        return repo.findAll();
    }

    public Activity save(Activity activity) {
        return repo.save(activity);
    }

    public Activity getById(Long id) {
        Optional<Activity> activity = repo.findById(id);
        return activity.orElse(null);
    }

    public void deleteById(Long id) {
        repo.deleteById(id);
    }

    public List<Activity> getByTags(List<String> tags) {
        return repo.findAll().stream()
                .filter(a -> a.getTags() != null && a.getTags().stream()
                        .anyMatch(tags::contains))
                .toList();
    }

    public List<Activity> searchByTitle(String title) {
        return repo.findAll().stream()
                .filter(a -> a.getTitle() != null && 
                           a.getTitle().toLowerCase().contains(title.toLowerCase()))
                .toList();
    }
}