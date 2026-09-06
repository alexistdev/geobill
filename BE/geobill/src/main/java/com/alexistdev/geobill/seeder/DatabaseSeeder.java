package com.alexistdev.geobill.seeder;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

/**
 * Runs every {@link Seeder} in order when the application starts.
 *
 * <p>This class knows nothing about the data itself. To add seed data, edit the matching
 * catalog or add a new {@link Seeder}; this file stays untouched.</p>
 */
@Slf4j
@Component
public class DatabaseSeeder implements CommandLineRunner {

    private final List<Seeder> seeders;

    public DatabaseSeeder(List<Seeder> seeders) {
        this.seeders = seeders.stream()
                .sorted(Comparator.comparingInt(Seeder::order))
                .toList();
    }

    @Override
    public void run(String... args) {
        log.info("Database seeding started, {} seeder(s) registered", seeders.size());

        for (Seeder seeder : seeders) {
            if (!seeder.shouldRun()) {
                log.info("Skipping seeder '{}', data already present", seeder.name());
                continue;
            }

            log.info("Running seeder '{}'", seeder.name());
            seeder.seed();
            log.info("Seeder '{}' finished", seeder.name());
        }

        log.info("Database seeding finished");
    }
}
