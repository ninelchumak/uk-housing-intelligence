package com.ukproptech.command.handlers;

import com.ukproptech.api.commands.UpdatePriceCommand;
import com.ukproptech.api.common.PropertyType;
import com.ukproptech.command.aggregates.PropertyAggregate;
import com.ukproptech.command.repositories.ListingRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.mockito.Mockito.*;

class UpdatePriceCommandHandlerTest {

    private ListingRepository repository;
    private UpdatePriceCommandHandler handler;

    @BeforeEach
    void setUp() {
        repository = mock(ListingRepository.class);
        // Хендлеру тепер потрібен ТІЛЬКИ репозиторій
        handler = new UpdatePriceCommandHandler(repository);
    }

    @Test
    void handle_ShouldUpdatePrice_WhenCommandIsValid() {
        // Arrange
        String propertyId = "1";
        BigDecimal oldPrice = BigDecimal.valueOf(500000);
        BigDecimal newPrice = BigDecimal.valueOf(550000);

        // Використовуємо реальний Агрегат, щоб він міг згенерувати подію
        PropertyAggregate listing = new PropertyAggregate(propertyId, "SW1A 1AA", oldPrice, PropertyType.FLAT);
        listing.clearChanges(); // Очищаємо початкову подію створення

        when(repository.findById(propertyId)).thenReturn(Optional.of(listing));

        UpdatePriceCommand command = new UpdatePriceCommand(propertyId, newPrice);

        // Act
        handler.handle(command);

        // Assert
        // Перевіряємо, що ціна в об'єкті змінилася
        Assertions.assertEquals(newPrice, listing.getPrice());

        // Перевіряємо, що репозиторій викликав save для всього агрегата
        verify(repository).save(listing);
    }

    @Test
    void handle_ShouldThrowException_WhenListingNotFound() {
        // Arrange
        String id = "999";
        UpdatePriceCommand command = new UpdatePriceCommand(id, BigDecimal.valueOf(550000));
        when(repository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> handler.handle(command)
        );
        Assertions.assertTrue(exception.getMessage().contains("not found"));

        verify(repository, never()).save(any());
    }
}