package com.ukproptech.command.repositories;

import com.ukproptech.command.aggregates.PropertyAggregate;

import java.util.Optional;

public interface ListingRepository {

    /**
     * Checks if a listing exists by its ID.
     *
     * @param id the ID of the listing
     * @return true if the listing exists, false otherwise
     */
    boolean existsById(String id);

    /**
     * Finds a listing by its ID and rehydrates its state.
     *
     * @param id the ID of the listing
     * @return an Optional containing the rehydrated aggregate if found
     */
    Optional<PropertyAggregate> findById(String id);

    /**
     * Saves the aggregate and persists its uncommitted events.
     *
     * @param property the aggregate to save
     */
    void save(PropertyAggregate property);
}