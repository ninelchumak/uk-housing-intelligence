package com.ukproptech.command.repositories.impl;

import com.ukproptech.command.aggregates.PropertyAggregate;
import com.ukproptech.command.infrastructure.EventStore;
import com.ukproptech.command.repositories.ListingRepository;

import java.util.List;
import java.util.Optional;

public class ListingRepositoryImpl implements ListingRepository {

    private final EventStore eventStore;

    public ListingRepositoryImpl(EventStore eventStore) {
        this.eventStore = eventStore;
    }

    @Override
    public void save(PropertyAggregate aggregate) {
        // We calculate expected version as current version minus number of new changes
        long expectedVersion = aggregate.getVersion() - aggregate.getChanges().size();

        eventStore.saveEvents(
                aggregate.getId(),
                expectedVersion,
                aggregate.getChanges()
        );

        aggregate.clearChanges();
    }

    @Override
    public Optional<PropertyAggregate> findById(String id) {
        List<Object> eventHistory = eventStore.getEvents(id);

        if (eventHistory.isEmpty()) {
            return Optional.empty();
        }

        PropertyAggregate property = new PropertyAggregate();

        // Rehydration
        for (Object event : eventHistory) {
            property.handleFromHistory(event);
        }

        return Optional.of(property);
    }

    @Override
    public boolean existsById(String id) {
        return !eventStore.getEvents(id).isEmpty();
    }
}
