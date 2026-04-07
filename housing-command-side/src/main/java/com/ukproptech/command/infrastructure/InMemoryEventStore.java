package com.ukproptech.command.infrastructure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * In-memory implementation of the EventStore interface.
 * Suitable for testing and development purposes.
 */
public class InMemoryEventStore implements EventStore {

    private final Map<String, List<Object>> eventStore = new HashMap<>();

    @Override
    public void save(Object event) {
        // Extract the aggregate ID from the event
        String aggregateId = extractAggregateId(event);

        // Add the event to the store
        eventStore.computeIfAbsent(aggregateId, id -> new ArrayList<>()).add(event);
    }

    @Override
    public List<Object> getEvents(String aggregateId) {
        return eventStore.getOrDefault(aggregateId, new ArrayList<>());
    }

    /**
     * Extracts the aggregate ID from an event.
     * Assumes the event has a method `id()` to retrieve the aggregate ID.
     *
     * @param event the event
     * @return the aggregate ID
     */
    private String extractAggregateId(Object event) {
        try {
            return (String) event.getClass().getMethod("id").invoke(event);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to extract aggregate ID from event", e);
        }
    }
}
