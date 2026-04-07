package com.ukproptech.command.aggregates;

import com.ukproptech.api.common.PropertyType;
import com.ukproptech.api.events.ListingCreatedEvent;
import com.ukproptech.api.events.PriceChangedEvent;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the aggregate root for a property listing.
 * Maintains the current state and applies changes only through events.
 */
public class PropertyAggregate {

    private final List<Object> changes = new ArrayList<>();
    private String id;
    private String postcode;
    private BigDecimal price;
    private PropertyType propertyType;

    public PropertyAggregate() {
        // Default constructor for rehydration
    }

    public PropertyAggregate(String id, String postcode, BigDecimal price, PropertyType propertyType) {
        applyChange(new ListingCreatedEvent(id, postcode, price, propertyType, Instant.now()));
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
            return; // No change
        }

        applyChange(new PriceChangedEvent(this.id, this.price, newPrice, Instant.now()));
    }

    /**
     * Applies a change and adds the event to the list of uncommitted changes.
     *
     * @param event the event to apply
     */
    private void applyChange(Object event) {
        if (event instanceof ListingCreatedEvent listingCreatedEvent) {
            apply(listingCreatedEvent);
        } else if (event instanceof PriceChangedEvent priceChangedEvent) {
            apply(priceChangedEvent);
        }
        changes.add(event);
    }

    public void handleFromHistory(Object event) {
        switch (event) {
            case ListingCreatedEvent e -> apply(e);
            case PriceChangedEvent e -> apply(e);
            case null -> throw new IllegalArgumentException("Event cannot be null");
            default -> System.out.println("Unknown event type: " + event.getClass());
        }
    }

    /**
     * Applies a ListingCreatedEvent to initialize the aggregate state.
     *
     * @param event the event to apply
     */
    private void apply(ListingCreatedEvent event) {
        this.id = event.id();
        this.postcode = event.postcode();
        this.price = event.price();
        this.propertyType = event.propertyType();
    }

    /**
     * Applies a PriceChangedEvent to update the aggregate state.
     *
     * @param event the event to apply
     */
    private void apply(PriceChangedEvent event) {
        this.price = event.newPrice();
    }

    /**
     * Returns the list of uncommitted changes.
     *
     * @return the list of uncommitted events
     */
    public List<Object> getChanges() {
        return new ArrayList<>(changes);
    }

    /**
     * Clears the list of uncommitted changes.
     */
    public void clearChanges() {
        changes.clear();
    }

    // Getters for the current state
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
}