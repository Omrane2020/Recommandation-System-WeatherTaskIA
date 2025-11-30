package com.example.weatheractivityapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.weatheractivityapi.model.Activity;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, Long> {
}