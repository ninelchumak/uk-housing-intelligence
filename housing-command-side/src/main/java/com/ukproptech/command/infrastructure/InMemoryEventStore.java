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
        store.compute(aggregateId, (id, eventStream) -> {
            List<Object> currentStream = (eventStream == null) ? new ArrayList<>() : eventStream;

            long currentVersion = currentStream.size();
            if (currentVersion != expectedVersion) {
                throw new ConcurrencyException(
                        "Conflict detected! Expected version " + expectedVersion + " but store has " + currentVersion
                );
            }
            currentStream.addAll(newEvents);
            return currentStream;
        });
        newEvents.forEach(eventBus::publish);
    }

    public List<Object> getEvents(String aggregateId) {
        return store.getOrDefault(aggregateId, Collections.emptyList());
    }
}