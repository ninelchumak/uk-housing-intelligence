package com.ukproptech.api.dto;

import java.math.BigDecimal;
/**
 * Represents analytics data for a specific area, including derived fields.
 * DTOs are used to transfer data to the query side for read operations.
 */
public record AreaAnalyticsDTO(
        String postcode,      // Можна використовувати Outcode (напр. "E1")
        BigDecimal averagePrice,
        BigDecimal rentYield,
        int commuteMinutes,
        Double crimeRate,     // Статистика від Police.uk
        String schoolRating   // Ofsted rating: "Outstanding", "Good" тощо
) {}