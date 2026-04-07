package com.ukproptech.api.dto;

import com.ukproptech.api.common.PropertyType;

import java.math.BigDecimal;

/**
 * Represents a summary of a property listing for the query side.
 * DTOs are used to transfer data to the query side for read operations.
 */
public record ListingSummaryDTO(
        String id,
        String postcode,
        BigDecimal price,
        PropertyType propertyType,
        Double rentYield,      // Calculated from Batch Job (DDIA Chapter 10)
        Integer commuteMinutes
) {
}