package com.ukproptech.api.commands;

import com.ukproptech.api.common.PropertyType;

import java.math.BigDecimal;

/**
 * Represents a command to create a new property listing.
 * Commands are used to express the intent to change the system's state.
 */
public record CreateListingCommand(String id, String postcode, BigDecimal price, PropertyType propertyType) {
}
