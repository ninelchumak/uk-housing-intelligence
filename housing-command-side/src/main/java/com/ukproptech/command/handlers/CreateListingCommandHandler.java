package com.ukproptech.command.handlers;

import com.ukproptech.api.commands.CreateListingCommand;
import com.ukproptech.command.aggregates.PropertyAggregate;
import com.ukproptech.command.repositories.ListingRepository;

public class CreateListingCommandHandler {
    private final ListingRepository repository;

    public CreateListingCommandHandler(ListingRepository repository) {
        this.repository = repository;
    }

    public void handle(CreateListingCommand command) {
        if (repository.existsById(command.id())) {
            throw new IllegalArgumentException("Listing with ID " + command.id() + " already exists.");
        }

        PropertyAggregate property = new PropertyAggregate(
                command.id(),
                command.postcode(),
                command.price(),
                command.propertyType()
        );

        repository.save(property);
    }
}
