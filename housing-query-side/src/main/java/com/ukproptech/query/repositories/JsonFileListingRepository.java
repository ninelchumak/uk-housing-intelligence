package com.ukproptech.query.repositories;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ukproptech.api.dto.PropertySummaryDTO;
import java.io.File;
import java.io.IOException;

/**
 * A file-persistent version of ListingQueryRepository.
 * It automatically flushes the in-memory state to property_view.json
 * every time a new summary is saved.
 */
public class JsonFileListingRepository extends ListingQueryRepository {
    private final String filePath = "property_view.json";
    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    /**
     * Saves the DTO to the internal map and triggers a file synchronization.
     */
    @Override
    public void save(PropertySummaryDTO dto) {
        // 1. Update the ConcurrentHashMap in the parent class
        super.save(dto);

        // 2. Persist the entire current state to disk
        syncToFile();
    }

    /**
     * Synchronizes the in-memory state with the JSON file.
     * Marked as synchronized to prevent concurrent write issues.
     */
    private synchronized void syncToFile() {
        try {
            // We fetch all records from the parent's map via findAll()
            var dataToSave = this.findAll();

            mapper.writerWithDefaultPrettyPrinter()
                    .writeValue(new File(filePath), dataToSave);

            System.out.println("[Query Side] property_view.json has been updated.");
        } catch (IOException e) {
            System.err.println("[Error] Could not sync to file: " + e.getMessage());
        }
    }
}