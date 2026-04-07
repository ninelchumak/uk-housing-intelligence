package com.ukproptech.api.events;

import java.math.BigDecimal;
import java.time.Instant;


/**
 * Represents an event indicating that the price of a property listing has been changed.
 * Events are immutable facts that describe what has happened in the system.
 */
public record PriceChangedEvent(String id, BigDecimal oldPrice, BigDecimal newPrice, Instant timestamp) {
}