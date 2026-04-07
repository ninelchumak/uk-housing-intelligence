package com.ukproptech.command.handlers;

import com.ukproptech.api.commands.UpdatePriceCommand;
import com.ukproptech.command.aggregates.PropertyAggregate;
import com.ukproptech.command.repositories.ListingRepository;

public class UpdatePriceCommandHandler {

    private final ListingRepository repository;

    public UpdatePriceCommandHandler(ListingRepository repository) {
        this.repository = repository;
    }

    public void handle(UpdatePriceCommand command) {
        // Rehydrate the aggregate
        PropertyAggregate property = repository.findById(command.id())
                .orElseThrow(() -> new IllegalArgumentException("Listing with ID " + command.id() + " not found."));

        // Delegate business logic to the aggregate
        property.updatePrice(command.newPrice());

        // Save the aggregate (repository handles persisting events)
        repository.save(property);
    }
}