package com.alexistdev.geobill.seeder.product;

import com.alexistdev.geobill.models.entity.Product;
import com.alexistdev.geobill.models.entity.ProductType;
import com.alexistdev.geobill.models.repository.ProductRepo;
import com.alexistdev.geobill.models.repository.ProductTypeRepo;
import com.alexistdev.geobill.seeder.SeedAudit;
import com.alexistdev.geobill.seeder.Seeder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Creates the products listed in {@link ProductCatalog#products()}.
 *
 * <p>Runs after {@link ProductTypeSeeder} because every product resolves its type by name.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProductSeeder implements Seeder {

    private final ProductRepo productRepo;
    private final ProductTypeRepo productTypeRepo;

    @Override
    public String name() {
        return "products";
    }

    @Override
    public int order() {
        return 40;
    }

    @Override
    public boolean shouldRun() {
        return productRepo.count() == 0;
    }

    @Override
    @Transactional
    public void seed() {
        List<Product> products = ProductCatalog.products().stream()
                .map(this::toProduct)
                .toList();

        productRepo.saveAll(products);
        log.info("Seeded {} product(s)", products.size());
    }

    private Product toProduct(ProductDefinition definition) {
        ProductType productType = productTypeRepo.findByNameIncludingDeleted(definition.productTypeName())
                .orElseThrow(() -> new IllegalStateException(
                        "Product '" + definition.name() + "' refers to unknown product type '"
                                + definition.productTypeName() + "'"));

        Product product = new Product();
        product.setName(definition.name());
        product.setProductType(productType);
        product.setPrice(definition.price());
        product.setCycle(definition.cycle());
        product.setCapacity(definition.capacity());
        product.setBandwith(definition.bandwith());
        product.setAddon_domain(definition.addonDomain());
        product.setDatabase_account(definition.databaseAccount());
        product.setFtp_account(definition.ftpAccount());
        product.setInfo1(definition.info1());
        product.setInfo2(definition.info2());
        product.setInfo3(definition.info3());
        product.setInfo4(definition.info4());
        product.setInfo5(definition.info5());
        return SeedAudit.stamp(product);
    }
}
