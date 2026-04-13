package com.ukproptech.query.services;

import com.ukproptech.api.dto.PropertySummaryDTO;
import com.ukproptech.query.repositories.ListingQueryRepository;

import java.util.Collection;
import java.util.Optional;

/**
 * Service providing a fast Read API for the UI or other services.
 * This is the "Query API" part of CQRS.
 */
public class PropertyQueryService {
    private final ListingQueryRepository repository;

    public PropertyQueryService(ListingQueryRepository repository) {
        this.repository = repository;
    }

    /**
     * Get all properties instantly from the fast Read Model.
     */
    public Collection<PropertySummaryDTO> getAllProperties() {
        return repository.findAll();
    }

    /**
     * Get a specific property by ID without replaying history.
     */
    public Optional<PropertySummaryDTO> getPropertyById(String id) {
        return repository.findById(id);
    }
}