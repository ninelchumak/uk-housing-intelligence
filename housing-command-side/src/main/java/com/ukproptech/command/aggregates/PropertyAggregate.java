package com.ukproptech.command.aggregates;

import com.ukproptech.api.common.PropertyType;
import com.ukproptech.api.events.PriceChangedEvent;
import com.ukproptech.api.events.PropertyCreatedEvent;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the aggregate root for a property listing.
 * Maintains the current state and applies changes only through versioned events.
 */
public class PropertyAggregate {

    private final List<Object> changes = new ArrayList<>();
    private String id;
    private String postcode;
    private BigDecimal price;
    private PropertyType propertyType;
    private long version = 0; // Tracks the current version of the aggregate

    public PropertyAggregate() {
        // Default constructor for rehydration from history
    }

    /**
     * Constructor for creating a brand new Property.
     * Initial version is always 1.
     */
    public PropertyAggregate(String id, String postcode, BigDecimal price, PropertyType propertyType) {
        // Version 1 is assigned upon creation
        applyChange(new PropertyCreatedEvent(id, postcode, price, propertyType, Instant.now(), 1));
    }

    /**
     * Updates the price of the property.
     *
     * @param newPrice the new price to set
     */
    public void updatePrice(BigDecimal newPrice) {
        if (newPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be greater than zero.");
        }
        if (newPrice.equals(this.price)) {
            return; // No change, no event generated
        }

        // New version is always current version + 1
        applyChange(new PriceChangedEvent(
                this.id,
                this.price,
                newPrice,
                Instant.now(),
                this.version + 1
        ));
    }

    /**
     * Handles event application and tracks uncommitted changes.
     */
    private void applyChange(Object event) {
        apply(event);
        changes.add(event);
    }

    /**
     * Restores the aggregate state from a list of historical events.
     * Use this during the "Rehydration" process.
     */
    public void handleFromHistory(Object event) {
        if (event == null) throw new IllegalArgumentException("Event cannot be null");
        apply(event);
    }

    /**
     * Internal dispatcher to route events to specific apply methods.
     * This keeps the state mutation logic clean and separated.
     */
    private void apply(Object event) {
        switch (event) {
            case PropertyCreatedEvent e -> applyInternal(e);
            case PriceChangedEvent e -> applyInternal(e);
            default -> System.out.println("Unknown event type: " + event.getClass());
        }
    }

    private void applyInternal(PropertyCreatedEvent event) {
        this.id = event.id();
        this.postcode = event.postcode();
        this.price = event.price();
        this.propertyType = event.type();
        this.version = event.version(); // Syncing aggregate version with event
    }

    private void applyInternal(PriceChangedEvent event) {
        this.price = event.newPrice();
        this.version = event.version(); // Syncing aggregate version with event
    }

    /**
     * Returns uncommitted changes that need to be persisted to the Event Store.
     */
    public List<Object> getChanges() {
        return new ArrayList<>(changes);
    }

    public void clearChanges() {
        changes.clear();
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getPostcode() {
        return postcode;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public PropertyType getPropertyType() {
        return propertyType;
    }

    public long getVersion() {
        return version;
    }
}