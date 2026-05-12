package com.ukproptech.command.infrastructure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class EventSerializer {
    private static final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    /**
     * Serializes an event object into a string format: "ClassName|JSON".
     * This format allows us to know the exact class type during restoration.
     *
     * @param event The domain event to serialize
     * @return A formatted string ready for the append-only log
     */
    public static String serialize(Object event) {
        try {
            String className = event.getClass().getCanonicalName();
            String json = mapper.writeValueAsString(event);
            return className + "|" + json;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize event", e);
        }
    }

    /**
     * Deserializes a log line back into a Java object.
     * Uses reflection to instantiate the correct class type based on the prefix.
     *
     * @param line The string line from the events.log file
     * @return The original event object
     */
    public static Object deserialize(String line) {
        try {
            // Split into 2 parts: [0] Class Name, [1] JSON Data
            String[] parts = line.split("\\|", 2);
            if (parts.length < 2) {
                throw new IllegalArgumentException("Invalid log line format");
            }

            Class<?> clazz = Class.forName(parts[0]);
            return mapper.readValue(parts[1], clazz);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize event line: " + line, e);
        }
    }
}
