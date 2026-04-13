package com.ukproptech.api.events;

import com.ukproptech.api.common.PropertyType;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Represents an event indicating that a new property listing has been created.
 * Events are immutable facts that describe what has happened in the system.
 */
public record PropertyCreatedEvent(
        String id,
        String postcode,
        BigDecimal price,
        PropertyType type,
        Instant timestamp,
        long version // Should always be 1 for a new listing
) {
}

