package com.alexistdev.geobill.seeder;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DatabaseSeederTest {

    private Seeder firstSeeder;
    private Seeder secondSeeder;

    @BeforeEach
    void setUp() {
        firstSeeder = mock(Seeder.class);
        secondSeeder = mock(Seeder.class);

        lenient().when(firstSeeder.name()).thenReturn("menus");
        lenient().when(firstSeeder.order()).thenReturn(10);
        lenient().when(secondSeeder.name()).thenReturn("users");
        lenient().when(secondSeeder.order()).thenReturn(20);
    }

    @Test
    @Order(1)
    @DisplayName("1:Test Run Executes Every Seeder That Should Run")
    void testRunExecutesEverySeeder() {
        when(firstSeeder.shouldRun()).thenReturn(true);
        when(secondSeeder.shouldRun()).thenReturn(true);

        DatabaseSeeder databaseSeeder = new DatabaseSeeder(List.of(firstSeeder, secondSeeder));
        databaseSeeder.run();

        verify(firstSeeder, times(1)).seed();
        verify(secondSeeder, times(1)).seed();
    }

    @Test
    @Order(2)
    @DisplayName("2:Test Run Skips Seeder When ShouldRun Is False")
    void testRunSkipsSeeder() {
        when(firstSeeder.shouldRun()).thenReturn(false);
        when(secondSeeder.shouldRun()).thenReturn(true);

        DatabaseSeeder databaseSeeder = new DatabaseSeeder(List.of(firstSeeder, secondSeeder));
        databaseSeeder.run();

        verify(firstSeeder, never()).seed();
        verify(secondSeeder, times(1)).seed();
    }

    @Test
    @Order(3)
    @DisplayName("3:Test Seeders Run In Ascending Order")
    void testSeedersRunInAscendingOrder() {
        when(firstSeeder.shouldRun()).thenReturn(true);
        when(secondSeeder.shouldRun()).thenReturn(true);

        DatabaseSeeder databaseSeeder = new DatabaseSeeder(List.of(secondSeeder, firstSeeder));
        databaseSeeder.run();

        InOrder inOrder = inOrder(firstSeeder, secondSeeder);
        inOrder.verify(firstSeeder).seed();
        inOrder.verify(secondSeeder).seed();
    }

    @Test
    @Order(4)
    @DisplayName("4:Test Run With No Seeder Registered")
    void testRunWithoutSeeder() {
        DatabaseSeeder databaseSeeder = new DatabaseSeeder(Collections.emptyList());

        Assertions.assertDoesNotThrow(() -> databaseSeeder.run());
    }

    @Test
    @Order(5)
    @DisplayName("5:Test Run Passes Command Line Arguments Without Failing")
    void testRunAcceptsArguments() {
        when(firstSeeder.shouldRun()).thenReturn(true);

        DatabaseSeeder databaseSeeder = new DatabaseSeeder(List.of(firstSeeder));
        databaseSeeder.run("--spring.profiles.active=test");

        verify(firstSeeder, times(1)).seed();
    }

    @Test
    @Order(6)
    @DisplayName("6:Test Failing Seeder Stops The Seeding")
    void testFailingSeederStopsSeeding() {
        when(firstSeeder.shouldRun()).thenReturn(true);
        org.mockito.Mockito.doThrow(new IllegalStateException("boom")).when(firstSeeder).seed();

        DatabaseSeeder databaseSeeder = new DatabaseSeeder(List.of(firstSeeder, secondSeeder));

        IllegalStateException exception =
                Assertions.assertThrows(IllegalStateException.class, databaseSeeder::run);

        Assertions.assertEquals("boom", exception.getMessage());
        verify(secondSeeder, never()).seed();
    }
}
