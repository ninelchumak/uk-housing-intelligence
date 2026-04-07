package com.ukproptech.api.events;

import com.ukproptech.api.common.PropertyType;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Represents an event indicating that a new property listing has been created.
 * Events are immutable facts that describe what has happened in the system.
 */
public record ListingCreatedEvent(String id, String postcode, BigDecimal price, PropertyType propertyType,
                                  Instant timestamp) {
}

