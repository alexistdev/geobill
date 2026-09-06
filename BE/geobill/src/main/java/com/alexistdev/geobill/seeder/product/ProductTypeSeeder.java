package com.alexistdev.geobill.seeder.product;

import com.alexistdev.geobill.models.entity.ProductType;
import com.alexistdev.geobill.models.repository.ProductTypeRepo;
import com.alexistdev.geobill.seeder.SeedAudit;
import com.alexistdev.geobill.seeder.Seeder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Creates the product types listed in {@link ProductCatalog#productTypes()}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProductTypeSeeder implements Seeder {

    private final ProductTypeRepo productTypeRepo;

    @Override
    public String name() {
        return "product types";
    }

    @Override
    public int order() {
        return 30;
    }

    @Override
    public boolean shouldRun() {
        return productTypeRepo.count() == 0;
    }

    @Override
    @Transactional
    public void seed() {
        List<ProductType> productTypes = ProductCatalog.productTypes().stream()
                .map(this::toProductType)
                .toList();

        productTypeRepo.saveAll(productTypes);
        log.info("Seeded {} product type(s)", productTypes.size());
    }

    private ProductType toProductType(String name) {
        ProductType productType = new ProductType();
        productType.setName(name);
        return SeedAudit.stamp(productType);
    }
}
