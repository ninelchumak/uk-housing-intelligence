package com.ukproptech.command.infrastructure;

import com.ukproptech.api.commands.exceptions.ConcurrencyException;
import com.ukproptech.api.infrastructure.EventBus;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryEventStore implements EventStore {
    // Map of aggregateId -> List of events
    private final Map<String, List<Object>> store = new ConcurrentHashMap<>();
    private final EventBus eventBus;

    public InMemoryEventStore(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    /**
     * Saves events to the store with version checking.
     * * @param aggregateId id of the property
     * @param expectedVersion the version the aggregate had BEFORE adding new events
     * @param newEvents list of events to append
     */
    public void saveEvents(String aggregateId, long expectedVersion, List<Object> newEvents) {
        List<Object> eventStream = store.getOrDefault(aggregateId, new ArrayList<>());

        // Check if the current version in store matches what the aggregate expected
        long currentVersion = eventStream.size();
        if (currentVersion != expectedVersion) {
            throw new ConcurrencyException(
                    "Conflict detected! Expected version " + expectedVersion + " but store has " + currentVersion
            );
        }

        // Append new events
        eventStream.addAll(newEvents);
        store.put(aggregateId, eventStream);
        newEvents.forEach(eventBus::publish);
    }

    public List<Object> getEvents(String aggregateId) {
        return store.getOrDefault(aggregateId, Collections.emptyList());
    }
}