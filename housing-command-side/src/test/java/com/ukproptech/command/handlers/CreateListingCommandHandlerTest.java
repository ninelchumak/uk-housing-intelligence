package com.ukproptech.command.handlers;

import com.ukproptech.api.commands.CreateListingCommand;
import com.ukproptech.api.common.PropertyType;
import com.ukproptech.command.repositories.ListingRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;

import static org.mockito.Mockito.*;

class CreateListingCommandHandlerTest {

    private ListingRepository repository;
    private CreateListingCommandHandler handler;

    @BeforeEach
    void setUp() {
        repository = mock(ListingRepository.class);
        handler = new CreateListingCommandHandler(repository);
    }

    @Test
    void handle_ShouldCreateListing_WhenCommandIsValid() {
        // Arrange
        String id = "1";
        CreateListingCommand command = new CreateListingCommand(id, "SW1A 1AA", BigDecimal.valueOf(500000), PropertyType.FLAT);
        when(repository.existsById(id)).thenReturn(false);

        // Act
        handler.handle(command);

        // Assert
        // Використовуємо ArgumentCaptor, щоб перевірити, що саме ми намагаємось зберегти
        var aggregateCaptor = ArgumentCaptor.forClass(com.ukproptech.command.aggregates.PropertyAggregate.class);
        verify(repository).save(aggregateCaptor.capture());

        var savedAggregate = aggregateCaptor.getValue();
        Assertions.assertEquals(id, savedAggregate.getId());
        Assertions.assertEquals(BigDecimal.valueOf(500000), savedAggregate.getPrice());
        // Перевіряємо, що всередині агрегата є подія створення
        Assertions.assertFalse(savedAggregate.getChanges().isEmpty());
    }

    @Test
    void handle_ShouldThrowException_WhenListingAlreadyExists() {
        // Arrange
        String id = "1";
        CreateListingCommand command = new CreateListingCommand(id, "SW1A 1AA", BigDecimal.valueOf(500000), PropertyType.FLAT);
        when(repository.existsById(id)).thenReturn(true);

        // Act & Assert
        Assertions.assertThrows(IllegalArgumentException.class, () -> handler.handle(command));

        verify(repository, never()).save(any());
    }
}