package com.ukproptech.command.infrastructure;

import com.ukproptech.api.infrastructure.EventBus;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class InMemoryEventBus implements EventBus {
    private final Map<Class<?>, List<Consumer<?>>> subscribers = new HashMap<>();

    @Override
    public void publish(Object event) {
        Class<?> eventClass = event.getClass();
        if (subscribers.containsKey(eventClass)) {
            subscribers.get(eventClass).forEach(handler -> {
                @SuppressWarnings("unchecked")
                Consumer<Object> consumer = (Consumer<Object>) handler;
                consumer.accept(event);
            });
        }
    }

    @Override
    public <T> void subscribe(Class<T> eventType, Consumer<T> handler) {
        subscribers.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>()).add(handler);
    }
}