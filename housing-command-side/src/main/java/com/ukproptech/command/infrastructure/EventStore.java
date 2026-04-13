package com.ukproptech.command.infrastructure;

import java.util.List;

public interface EventStore {

    /**
     * Persists a list of events to the store after verifying the aggregate's version.
     *
     * @param aggregateId     The unique identifier of the property.
     * @param expectedVersion The version that the application state was based on.
     * @param events          The new events to be appended to the stream.
     * @throws com.ukproptech.command.exceptions.ConcurrencyException if versions mismatch.
     */
    void saveEvents(String aggregateId, long expectedVersion, List<Object> events);

    /**
     * Retrieves the entire event stream for a specific aggregate to reconstruct its state.
     *
     * @param aggregateId The unique identifier of the property.
     * @return A list of historical events in chronological order.
     */
    List<Object> getEvents(String aggregateId);
}