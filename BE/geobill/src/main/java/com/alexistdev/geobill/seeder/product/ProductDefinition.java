package com.alexistdev.geobill.seeder.product;

import lombok.Builder;

/**
 * One product as written in {@link ProductCatalog}.
 *
 * <p>Built with a builder so only the fields that matter for a package have to be listed,
 * and each value is read next to its own name.</p>
 */
@Builder
public record ProductDefinition(
        String productTypeName,
        String name,
        double price,
        int cycle,
        String capacity,
        String bandwith,
        String addonDomain,
        String databaseAccount,
        String ftpAccount,
        String info1,
        String info2,
        String info3,
        String info4,
        String info5
) {
}
