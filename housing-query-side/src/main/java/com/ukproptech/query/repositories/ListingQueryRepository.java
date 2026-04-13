package com.ukproptech.query.repositories;

import com.ukproptech.api.dto.PropertySummaryDTO;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class ListingQueryRepository {
    private final ConcurrentHashMap<String, PropertySummaryDTO> readModel = new ConcurrentHashMap<>();

    public void save(PropertySummaryDTO dto) {
        readModel.put(dto.id(), dto);
    }

    public Optional<PropertySummaryDTO> findById(String id) {
        return Optional.ofNullable(readModel.get(id));
    }

    public Collection<PropertySummaryDTO> findAll() {
        return readModel.values();
    }
}