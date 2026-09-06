package com.alexistdev.geobill.seeder;

/**
 * One unit of seeding (menus, users, products, ...).
 *
 * <p>To add a new one, create a {@code @Component} that implements this interface.
 * {@link DatabaseSeeder} picks it up automatically, no other file needs to change.</p>
 */
public interface Seeder {

    /** Name shown in the log, e.g. "menus". */
    String name();

    /** Execution order; lower runs first. */
    int order();

    /** The seeder only runs when this returns true, usually a check that the table is empty. */
    boolean shouldRun();

    /** Writes the initial data. */
    void seed();
}
