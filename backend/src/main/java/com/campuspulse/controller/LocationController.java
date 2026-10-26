package com.campuspulse.controller;

import com.campuspulse.entity.LocationItem;
import com.campuspulse.repository.LocationRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
public class LocationController {

    private final LocationRepository locationRepository;

    public LocationController(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    @GetMapping
    public ResponseEntity<List<LocationItem>> getLocations() {
        return ResponseEntity.ok(locationRepository.findAll());
    }
}
