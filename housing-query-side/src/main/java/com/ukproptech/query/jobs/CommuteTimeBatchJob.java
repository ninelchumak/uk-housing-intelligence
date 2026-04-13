package com.ukproptech.query.jobs;

import com.ukproptech.query.repositories.ListingQueryRepository;
import com.ukproptech.api.dto.PropertySummaryDTO;

import java.util.Random;

/**
 * Simulates a background batch process that enriches data.
 * In a real system, this would call an external API like Google Maps.
 */
public class CommuteTimeBatchJob {
    private final ListingQueryRepository repository;
    private final Random random = new Random();

    public CommuteTimeBatchJob(ListingQueryRepository repository) {
        this.repository = repository;
    }

    /**
     * Finds all properties with missing commute time and updates them.
     */
    public void run() {
        // Fetching all records from the read model
        repository.findAll().stream()
                .filter(p -> p.commuteMinutes() == 0) // Only process new/unprocessed items
                .forEach(this::enrichCommuteTime);
    }

    private void enrichCommuteTime(PropertySummaryDTO property) {
        // Simulating external API call to calculate distance to London Center
        int simulatedMinutes = 15 + random.nextInt(45); // Random time between 15 and 60 mins

        PropertySummaryDTO enriched = new PropertySummaryDTO(
                property.id(),
                property.postcode(),
                property.price(),
                property.propertyType(),
                property.rentYield(),
                simulatedMinutes // Setting the calculated value
        );

        // Update the Read Model with enriched data
        repository.save(enriched);

        System.out.println("Batch Job: Enriched property " + property.id() +
                " with commute time: " + simulatedMinutes + " mins.");
    }
}