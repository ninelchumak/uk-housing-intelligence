package com.ukproptech.command.infrastructure;


import java.util.List;

/**
 * Interface for an event store that persists and retrieves events.
 */
public interface EventStore {

    /**
     * Saves an event to the store.
     *
     * @param event the event to save
     */
    void save(Object event);

    /**
     * Retrieves all events for a given aggregate ID.
     *
     * @param aggregateId the ID of the aggregate
     * @return a list of events for the aggregate
     */
    List<Object> getEvents(String aggregateId);
}
