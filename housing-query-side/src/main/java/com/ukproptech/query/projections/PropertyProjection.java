package com.ukproptech.query.projections;

import com.ukproptech.api.dto.PropertySummaryDTO;
import com.ukproptech.api.events.PropertyCreatedEvent;
import com.ukproptech.api.events.PriceChangedEvent;
import com.ukproptech.query.repositories.ListingQueryRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class PropertyProjection {
    private final ListingQueryRepository repository;

    public PropertyProjection(ListingQueryRepository repository) {
        this.repository = repository;
    }

    public void on(Object event) {
        switch (event) {
            case PropertyCreatedEvent e -> handleCreated(e);
            case PriceChangedEvent e -> handlePriceChanged(e);
            default -> System.out.println("Unknown event: " + event.getClass().getSimpleName());
        }
    }

    private void handleCreated(PropertyCreatedEvent e) {
        var dto = new PropertySummaryDTO(
                e.id(),
                e.postcode(),
                e.price(),
                e.type(),
                0.0,
                0
        );
        repository.save(dto);
    }

    private void handlePriceChanged(PriceChangedEvent e) {
        repository.findById(e.id()).ifPresent(old -> {
            Double updatedYield = calculateNewYield(old.rentYield(), old.price(), e.newPrice());

            var updated = new PropertySummaryDTO(
                    old.id(),
                    old.postcode(),
                    e.newPrice(),
                    old.propertyType(),
                    updatedYield,
                    old.commuteMinutes()
            );
            repository.save(updated);
        });
    }

    // Business logic: recalculate rental yield based on inverse price correlation.
    private Double calculateNewYield(Double oldYield, BigDecimal oldPrice, BigDecimal newPrice) {
        if (oldYield == null || oldYield == 0 || oldPrice.compareTo(BigDecimal.ZERO) == 0) return 0.0;
        return oldPrice.multiply(BigDecimal.valueOf(oldYield))
                .divide(newPrice, 2, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
