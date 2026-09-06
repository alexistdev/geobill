package com.alexistdev.geobill.seeder.product;

import java.util.List;

/**
 * The single place where product types and products are declared.
 *
 * <p>Add a product type by adding its name to {@link #productTypes()}, and a product by adding
 * one builder block to {@link #products()}. {@code productTypeName} must match a name listed in
 * {@link #productTypes()}.</p>
 */
public final class ProductCatalog {

    public static final String SHARED_HOSTING = "Shared Hosting";
    public static final String VPS = "VPS";

    private static final String SUPPORTED_LANGUAGES = "PHP, Ruby, Python, NodeJS";

    private ProductCatalog() {
    }

    public static List<String> productTypes() {
        return List.of(SHARED_HOSTING, VPS);
    }

    public static List<ProductDefinition> products() {
        return List.of(
                ProductDefinition.builder()
                        .productTypeName(SHARED_HOSTING)
                        .name("NVME-1")
                        .price(50_000.0)
                        .cycle(12)
                        .capacity("10GB")
                        .bandwith("1000 Mbps")
                        .addonDomain("1")
                        .databaseAccount("1")
                        .ftpAccount("1")
                        .info1("1 GB RAM")
                        .info2("2 Core")
                        .info3(SUPPORTED_LANGUAGES)
                        .build(),

                ProductDefinition.builder()
                        .productTypeName(SHARED_HOSTING)
                        .name("NVME-2")
                        .price(150_000.0)
                        .cycle(12)
                        .capacity("40GB")
                        .bandwith("5000 Mbps")
                        .addonDomain("1")
                        .databaseAccount("1")
                        .ftpAccount("1")
                        .info1("4 GB RAM")
                        .info2("4 Core")
                        .info3(SUPPORTED_LANGUAGES)
                        .build(),

                ProductDefinition.builder()
                        .productTypeName(SHARED_HOSTING)
                        .name("NVME-3")
                        .price(200_000.0)
                        .cycle(12)
                        .capacity("80GB")
                        .bandwith("Unlimited")
                        .addonDomain("Unlimited")
                        .databaseAccount("Unlimited")
                        .ftpAccount("5")
                        .info1("6 GB RAM")
                        .info2("8 Core")
                        .info3(SUPPORTED_LANGUAGES)
                        .build()
        );
    }
}
