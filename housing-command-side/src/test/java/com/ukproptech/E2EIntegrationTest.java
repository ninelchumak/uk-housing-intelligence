package com.ukproptech;

import com.ukproptech.api.commands.CreateListingCommand;
import com.ukproptech.api.commands.UpdatePriceCommand;
import com.ukproptech.api.common.PropertyType;
import com.ukproptech.command.aggregates.PropertyAggregate;
import com.ukproptech.command.handlers.CreateListingCommandHandler;
import com.ukproptech.command.handlers.UpdatePriceCommandHandler;
import com.ukproptech.command.infrastructure.InMemoryEventStore;
import com.ukproptech.command.repositories.ListingRepository;
import com.ukproptech.command.repositories.impl.ListingRepositoryImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class E2EIntegrationTest {

    private InMemoryEventStore eventStore;
    private ListingRepository repository;
    private CreateListingCommandHandler createHandler;
    private UpdatePriceCommandHandler updateHandler;

    @BeforeEach
    void setUp() {
        eventStore = new InMemoryEventStore();
        repository = new ListingRepositoryImpl(eventStore);
        createHandler = new CreateListingCommandHandler(repository);
        updateHandler = new UpdatePriceCommandHandler(repository);
    }

    @Test
    void shouldCreateAndThenUpdatePriceSuccessfully() {
        String propId = "test-prop-123";
        CreateListingCommand createCmd = new CreateListingCommand(propId, "SW1A 1AA", new BigDecimal("500000"), PropertyType.FLAT);

        createHandler.handle(createCmd);

        assertEquals(1, eventStore.getEvents(propId).size(), "Should have 1 event after creation");

        BigDecimal newPrice = new BigDecimal("550000");
        UpdatePriceCommand updateCmd = new UpdatePriceCommand(propId, newPrice);

        updateHandler.handle(updateCmd);

        PropertyAggregate restored = repository.findById(propId).orElseThrow(() -> new AssertionError("Aggregate was not found!"));

        assertAll("Checking restored aggregate state", () -> assertEquals(propId, restored.getId()), () -> assertEquals(newPrice, restored.getPrice(), "Price should be updated"), () -> assertEquals("SW1A 1AA", restored.getPostcode()), () -> assertEquals(2, eventStore.getEvents(propId).size(), "Should be 2 events"));

        System.out.println("History of events for " + propId + ":");
        eventStore.getEvents(propId).forEach(e -> System.out.println(" - " + e.getClass().getSimpleName()));
    }
}