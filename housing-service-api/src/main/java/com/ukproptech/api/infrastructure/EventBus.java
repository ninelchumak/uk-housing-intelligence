package com.ukproptech.api.infrastructure;

import java.util.function.Consumer;

public interface EventBus {
    // Publish an event to everyone who is listening
    void publish(Object event);

    // Subscribe to events of a specific type
    <T> void subscribe(Class<T> eventType, Consumer<T> handler);
}