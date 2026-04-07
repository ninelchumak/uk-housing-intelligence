package com.ukproptech.api.commands;

import java.math.BigDecimal;

/**
 * Represents a command to update the price of an existing property listing.
 * Commands are used to express the intent to change the system's state.
 */
public record UpdatePriceCommand(String id, BigDecimal newPrice) {
}
