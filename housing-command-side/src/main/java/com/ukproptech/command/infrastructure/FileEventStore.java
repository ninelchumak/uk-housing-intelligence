package com.ukproptech.command.infrastructure;


import com.ukproptech.api.commands.exceptions.ConcurrencyException;
import com.ukproptech.api.infrastructure.EventBus;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A production-ready file-based Event Store.
 * Implements Append-only logging and Hash Indexing for O(1) access.
 * Supports durability: reloads indices from the log file on startup.
 */
public class FileEventStore implements EventStore {
    private final String storagePath = "events.log";

    // Hash Index: Maps Aggregate ID to its first occurrence (byte offset) in the file
    private final Map<String, Long> index = new ConcurrentHashMap<>();
    private final EventBus eventBus;

    public FileEventStore(EventBus eventBus) {
        this.eventBus = eventBus;
        ensureFileExists();
        rebuildIndex(); // Essential for Durability: restores state from disk
    }

    /**
     * Appends events to the log with Optimistic Concurrency check.
     */
    @Override
    public synchronized void saveEvents(String aggregateId, long expectedVersion, List<Object> newEvents) {
        // 1. Concurrency Check (DDIA Chapter 7)
        List<Object> currentHistory = getEvents(aggregateId);
        long currentVersion = currentHistory.size();

        if (currentVersion != expectedVersion) {
            // Throw your specific domain exception instead of generic RuntimeException
            throw new ConcurrencyException(
                    "Concurrency Conflict! Expected version " + expectedVersion + " but store has " + currentVersion
            );
        }

        // 2. Persistent Write (Append-only)
        try (RandomAccessFile raf = new RandomAccessFile(storagePath, "rw")) {
            long filePointer = raf.length();
            raf.seek(filePointer);

            for (Object event : newEvents) {
                String serialized = EventSerializer.serialize(event) + "\n";
                raf.write(serialized.getBytes(StandardCharsets.UTF_8));
            }

            // Update Index only if this is the first time we see this aggregate
            index.putIfAbsent(aggregateId, filePointer);

        } catch (IOException e) {
            throw new RuntimeException("Disk I/O error during event persistence", e);
        }

        // 3. Volatile Notification
        newEvents.forEach(eventBus::publish);
    }

    /**
     * Reads events from the file starting from the indexed offset.
     */
    @Override
    public List<Object> getEvents(String aggregateId) {
        List<Object> history = new ArrayList<>();
        Long offset = index.get(aggregateId);

        if (offset == null) return history;

        try (RandomAccessFile raf = new RandomAccessFile(storagePath, "r")) {
            raf.seek(offset);
            String line;
            while ((line = raf.readLine()) != null) {
                Object event = EventSerializer.deserialize(line);
                if (extractIdFromEvent(event).equals(aggregateId)) {
                    history.add(event);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading event stream for: " + aggregateId, e);
        }
        return history;
    }

    /**
     * Scans the entire events.log to rebuild the in-memory index.
     * This is what makes the system 'Stateful' across restarts.
     */
    private void rebuildIndex() {
        try (RandomAccessFile raf = new RandomAccessFile(storagePath, "r")) {
            long currentPointer = 0;
            String line;
            while ((line = raf.readLine()) != null) {
                try {
                    Object event = EventSerializer.deserialize(line);
                    String id = extractIdFromEvent(event);
                    // Only store the very first pointer for each ID
                    index.putIfAbsent(id, currentPointer);
                } catch (Exception e) {
                    // Skip corrupted lines or unknown event types
                }
                currentPointer = raf.getFilePointer();
            }
        } catch (IOException e) {
            // File might be empty, which is fine
        }
    }

    private void ensureFileExists() {
        try {
            File file = new File(storagePath);
            if (!file.exists()) file.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException("Initialization failed", e);
        }
    }

    private String extractIdFromEvent(Object event) {
        try {
            // Using reflection to call id() method from Records or getId() from Classes
            return (String) event.getClass().getMethod("id").invoke(event);
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Clears the storage file and the in-memory index.
     * Useful for test isolation.
     */
    public void clear() {
        index.clear();
        try (PrintWriter writer = new PrintWriter(storagePath)) {
            writer.print(""); // Effectively wipes the file content
        } catch (IOException e) {
            throw new RuntimeException("Failed to clear event store file", e);
        }
    }
}