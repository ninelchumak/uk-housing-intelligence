package com.ukproptech;

import com.ukproptech.api.commands.CreateListingCommand;
import com.ukproptech.api.commands.UpdatePriceCommand;
import com.ukproptech.api.commands.exceptions.ConcurrencyException;
import com.ukproptech.api.common.PropertyType;
import com.ukproptech.api.dto.PropertySummaryDTO;
import com.ukproptech.api.events.PriceChangedEvent;
import com.ukproptech.api.events.PropertyCreatedEvent;
import com.ukproptech.api.infrastructure.EventBus;
import com.ukproptech.command.aggregates.PropertyAggregate;
import com.ukproptech.command.handlers.CreateListingCommandHandler;
import com.ukproptech.command.handlers.UpdatePriceCommandHandler;
import com.ukproptech.command.infrastructure.FileEventStore;
import com.ukproptech.command.infrastructure.InMemoryEventBus;
import com.ukproptech.command.infrastructure.InMemoryEventStore;
import com.ukproptech.command.repositories.ListingRepository;
import com.ukproptech.command.repositories.impl.ListingRepositoryImpl;
import com.ukproptech.query.jobs.CommuteTimeBatchJob;
import com.ukproptech.query.projections.PropertyProjection;
import com.ukproptech.query.repositories.JsonFileListingRepository;
import com.ukproptech.query.repositories.ListingQueryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class E2EIntegrationTest {
    private FileEventStore eventStore;
    private ListingRepository repository;
    private CreateListingCommandHandler createHandler;
    private UpdatePriceCommandHandler updateHandler;
    private EventBus eventBus;
    private ListingQueryRepository queryRepository;
    private PropertyProjection projection;

    @BeforeEach
    void setUp() {
        eventBus = new InMemoryEventBus();
        eventStore = new FileEventStore(eventBus);
        eventStore.clear(); // Now every test starts with a fresh file

        repository = new ListingRepositoryImpl(eventStore);
        createHandler = new CreateListingCommandHandler(repository);
        updateHandler = new UpdatePriceCommandHandler(repository);

        queryRepository = new JsonFileListingRepository();
        projection = new PropertyProjection(queryRepository);

        eventBus.subscribe(PropertyCreatedEvent.class, projection::on);
        eventBus.subscribe(PriceChangedEvent.class, projection::on);
    }

    @Test
    @DisplayName("Should create property with v1 and update to v2")
    void shouldCreateAndThenUpdatePriceSuccessfully() {
        String propId = "test-prop-123";
        CreateListingCommand createCmd = new CreateListingCommand(propId, "SW1A 1AA", new BigDecimal("500000"), PropertyType.FLAT);

        // Act: Create
        createHandler.handle(createCmd);

        // Verify version 1 after creation
        assertEquals(1, eventStore.getEvents(propId).size(), "Should have 1 event after creation");

        BigDecimal newPrice = new BigDecimal("550000");
        UpdatePriceCommand updateCmd = new UpdatePriceCommand(propId, newPrice);

        // Act: Update
        updateHandler.handle(updateCmd);

        // Assert: Verify aggregate state and versioning
        PropertyAggregate restored = repository.findById(propId)
                .orElseThrow(() -> new AssertionError("Aggregate was not found!"));

        assertAll("Checking restored aggregate state and OCC version",
                () -> assertEquals(propId, restored.getId()),
                () -> assertEquals(newPrice, restored.getPrice(), "Price should be updated"),
                () -> assertEquals(2, restored.getVersion(), "Aggregate version should be 2 after one update"),
                () -> assertEquals(2, eventStore.getEvents(propId).size(), "Event store should contain 2 events")
        );
    }

    @Test
    @DisplayName("Batch Job should enrich property data with commute time")
    void batchJobEnrichmentTest() {
        // 1. Setup
        var batchJob = new CommuteTimeBatchJob(queryRepository);
        String propId = "batch-test-1";

        // 2. Initial State: Property exists but has 0 commute minutes
        queryRepository.save(new PropertySummaryDTO(
                propId, "E1 6AN", new BigDecimal("600000"), PropertyType.FLAT, 4.5, 0
        ));

        // 3. ACT: Run the background job
        batchJob.run();

        // 4. ASSERT: Verify that commute minutes are no longer 0
        var updated = queryRepository.findById(propId).orElseThrow();

        assertAll("Verify Batch Enrichment",
                () -> assertTrue(updated.commuteMinutes() > 0, "Commute minutes should be enriched"),
                () -> assertEquals(4.5, updated.rentYield(), "Other data should remain intact")
        );
    }

    @Test
    @DisplayName("Should throw ConcurrencyException when versions mismatch")
    void shouldDetectConcurrencyConflict() {
        String propId = "conflict-1";

        // 1. Setup: initial creation (Version 1)
        createHandler.handle(new CreateListingCommand(propId, "W1", new BigDecimal("100"), PropertyType.FLAT));

        // 2. Simulate two people loading the same aggregate
        PropertyAggregate userA_view = repository.findById(propId).get();
        PropertyAggregate userB_view = repository.findById(propId).get();

        // 3. User A updates price (Version becomes 2)
        userA_view.updatePrice(new BigDecimal("150"));
        repository.save(userA_view); // Success!

        // 4. User B tries to update price from his old view (still thinks version is 1)
        userB_view.updatePrice(new BigDecimal("200"));

        // This should fail because the store now has version 2
        assertThrows(ConcurrencyException.class, () -> {
            repository.save(userB_view);
        }, "Should fail because User A already updated this property");
    }

    @Test
    @DisplayName("Automatic projection update via Event Bus")
    void shouldSynchronizeReadModelViaEventBus() {
        String propId = "bus-123";

        createHandler.handle(new CreateListingCommand(propId, "NW1", new BigDecimal("800000"), PropertyType.FLAT));

        assertTrue(queryRepository.findById(propId).isPresent());
    }

    @Test
    @DisplayName("Should handle long event stream and persist 10+ events in log")
    void shouldHandleLongEventStream() {
        String propId = "dynamic-price-prop";
        BigDecimal initialPrice = new BigDecimal("100000");

        // 1. Initial creation (Event #1)
        createHandler.handle(new CreateListingCommand(propId, "E14", initialPrice, PropertyType.HOUSE));

        // 2. Perform 9 subsequent updates (Events #2 to #10)
        // This simulates a bidding war or dynamic market updates
        for (int i = 1; i <= 9; i++) {
            BigDecimal updatedPrice = initialPrice.add(new BigDecimal(i * 10000));
            updateHandler.handle(new UpdatePriceCommand(propId, updatedPrice));
        }

        // 3. ASSERT: Verify physical storage
        assertEquals(10, eventStore.getEvents(propId).size(),
                "Event store should contain exactly 10 events for this property");

        // 4. ASSERT: Verify Rehydration (State recovery from 10 events)
        PropertyAggregate restored = repository.findById(propId)
                .orElseThrow(() -> new AssertionError("Aggregate should be restorable from long history"));

        BigDecimal finalExpectedPrice = initialPrice.add(new BigDecimal("90000")); // 100k + 90k

        assertAll("Verify state after 10 events",
                () -> assertEquals(10, restored.getVersion(), "Aggregate version should be 10"),
                () -> assertEquals(finalExpectedPrice, restored.getPrice(), "Final price should be correctly calculated"),
                () -> assertEquals("E14", restored.getPostcode(), "Original data should still be intact")
        );

        // Optional: Print to console so you can see it in the log file
        System.out.println("History for " + propId + " successfully written to events.log (10 entries)");
    }
}