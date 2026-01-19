package com.alexistdev.geobill.models.repository;

import com.alexistdev.geobill.models.entity.BaseEntity;
import com.alexistdev.geobill.models.entity.Product;
import com.alexistdev.geobill.models.entity.ProductType;
import com.alexistdev.geobill.models.entity.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@DataJpaTest
@ActiveProfiles("test")
public class ProductRepoTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ProductRepo productRepo;

    @Autowired
    private ProductTypeRepo productTypeRepo;

    private static final String SYSTEM_USER = "System";

    private ProductType productType;

    @BeforeEach
    void setUp() {
        User testUser = new User();
        testUser.setEmail("testUser@gmail.com");
        testUser.setPassword("password");
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(testUser, null, new ArrayList<>());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        productType = new ProductType();
        productType.setName("Shared Hosting");
        productType.setCreatedBy(SYSTEM_USER);
        productType.setCreatedDate(new java.util.Date());
        productType.setModifiedBy(SYSTEM_USER);
        productType.setModifiedDate(new java.util.Date());
        productType.setIsDeleted(false);
        entityManager.persist(productType);
        entityManager.flush();
    }

    private Product createProduct(String name, ProductType type, Double price, Integer cycle,
                                  String capacity, String bandwith, String addOnDomain,
                                  String database, String ftp) {
        Product createdProduct = new Product();
        createdProduct.setName(name);
        createdProduct.setProductType(type);
        createdProduct.setPrice(price);
        createdProduct.setCycle(cycle);
        createdProduct.setCapacity(capacity);
        createdProduct.setBandwith(bandwith);
        createdProduct.setAddon_domain(addOnDomain);
        createdProduct.setDatabase_account(database);
        createdProduct.setFtp_account(ftp);
        createdProduct.setCreatedBy(SYSTEM_USER);
        createdProduct.setCreatedDate(new java.util.Date());
        createdProduct.setModifiedBy(SYSTEM_USER);
        createdProduct.setModifiedDate(new java.util.Date());
        createdProduct.setIsDeleted(false);
        return createdProduct;
    }

    @Test
    @DisplayName("Test Save Product")
    void testSaveProduct() {
        Product product = createProduct("Basic Shared Hosting",
                productType, 10.0, 1, "10GB",
                "1000 Mbps", "1", "1", "1");

        Product savedProduct = productRepo.save(product);

        Assertions.assertNotNull(savedProduct);
        Assertions.assertEquals(product.getName(), savedProduct.getName());
        Assertions.assertEquals(product.getProductType().getName(), savedProduct.getProductType().getName());
        Assertions.assertEquals(product.getPrice(), savedProduct.getPrice());
        Assertions.assertEquals(product.getCycle(), savedProduct.getCycle());
        Assertions.assertEquals(product.getCapacity(), savedProduct.getCapacity());
        Assertions.assertEquals(product.getBandwith(), savedProduct.getBandwith());
        Assertions.assertEquals(product.getAddon_domain(), savedProduct.getAddon_domain());
        Assertions.assertEquals(product.getDatabase_account(), savedProduct.getDatabase_account());
        Assertions.assertEquals(product.getFtp_account(), savedProduct.getFtp_account());
        Assertions.assertNotNull(savedProduct.getCreatedDate());
        Assertions.assertEquals(product.getModifiedBy(), savedProduct.getModifiedBy());
    }

    @Test
    @DisplayName("Test Find By Product UUID")
    void testFindByProductUUID() {
        Product product = createProduct("Basic Shared Hosting",
                productType, 10.0, 1, "10GB",
                "1000 Mbps", "1", "1", "1");
        entityManager.persist(product);
        entityManager.flush();
        Product foundProduct = productRepo.findById(product.getId()).orElse(null);
        Assertions.assertNotNull(foundProduct);
        Assertions.assertEquals(product.getName(), foundProduct.getName());
    }

    @Test
    @DisplayName("Test Find All Products")
    void testFindAllProducts() {
        Product product1 = createProduct("Basic Shared Hosting",
                productType, 10.0, 1, "10GB",
                "1000 Mbps", "1", "1", "1");
        entityManager.persist(product1);
        entityManager.flush();

        Product product2 = createProduct("VPS",
                productType, 100.0, 12, "100GB",
                "10000 Mbps", "5", "5", "5");
        entityManager.persist(product2);
        entityManager.flush();

        List<Product> foundProducts = productRepo.findAll();
        Assertions.assertNotNull(foundProducts);
        Assertions.assertEquals(2, foundProducts.size());
    }

    @Test
    @DisplayName("Test Delete Product")
    void testDeleteProduct() {
        Product product = createProduct("Basic Shared Hosting",
                productType, 10.0, 1, "10GB",
                "1000 Mbps", "1", "1", "1");
        entityManager.persist(product);
        entityManager.flush();

        productRepo.delete(product);
        entityManager.flush();
        entityManager.clear();

        List<Product> allProducts = productRepo.findAll();
        Assertions.assertEquals(0, allProducts.size());

        //validate soft deleted
        Object isDeleted = entityManager.getEntityManager()
                .createNativeQuery("SELECT is_deleted FROM tb_products WHERE uuid = ?1")
                .setParameter(1, product.getId())
                .getSingleResult();

        boolean isDeletedBool = false;
        if (isDeleted instanceof Boolean) {
            isDeletedBool = (Boolean) isDeleted;
        } else if (isDeleted instanceof Number) {
            isDeletedBool = ((Number) isDeleted).intValue() == 1;
        }

        Assertions.assertTrue(isDeletedBool,
                "Product should be soft-deleted in the database");

    }

    @Test
    @DisplayName("Test Delete All Products")
    void testDeleteAllProducts() {
        Product product1 = createProduct("Basic Shared Hosting",
                productType, 10.0, 1, "10GB",
                "1000 Mbps", "1", "1", "1");
        entityManager.persist(product1);
        entityManager.flush();

        Product product2 = createProduct("VPS",
                productType, 100.0, 12, "100GB",
                "10000 Mbps", "5", "5", "5");
        entityManager.persist(product2);
        entityManager.flush();

        productRepo.deleteAll(List.of(product1, product2));
        entityManager.flush();
        entityManager.clear();

        List<Product> allProducts = productRepo.findAll();
        Assertions.assertEquals(0, allProducts.size());

        //validate soft deleted
        List<?> results = entityManager.getEntityManager()
                .createNativeQuery("SELECT is_deleted FROM tb_products WHERE uuid IN (?1, ?2)")
                .setParameter(1, product1.getId())
                .setParameter(2, product2.getId())
                .getResultList();

        Assertions.assertEquals(2, results.size(), "Both products should still exist in the DB");

        for (Object isDeleted : results) {
            boolean isDeletedBool = false;
            if (isDeleted instanceof Boolean) {
                isDeletedBool = (Boolean) isDeleted;
            } else if (isDeleted instanceof Number) {
                isDeletedBool = ((Number) isDeleted).intValue() == 1;
            }
            Assertions.assertTrue(isDeletedBool, "Product should be soft-deleted");
        }
    }

    @Test
    @DisplayName("Test Find By Name Including Deleted")
    void testFindByNameIncludingDeleted() {
        Product product1 = createProduct("Basic Shared Hosting",
                productType, 10.0, 1, "10GB",
                "1000 Mbps", "1", "1", "1");
        entityManager.persist(product1);
        entityManager.flush();

        Product product2 = createProduct("VPS",
                productType, 100.0, 12, "100GB",
                "10000 Mbps", "5", "5", "5");
        entityManager.persist(product2);
        entityManager.flush();
        entityManager.clear();

        Optional<Product> foundProduct1 = productRepo.findByNameIncludingDeleted("Basic Shared Hosting");
        Assertions.assertTrue(foundProduct1.isPresent());
        Assertions.assertEquals("Basic Shared Hosting", foundProduct1.get().getName());
        Assertions.assertFalse(foundProduct1.get().getIsDeleted());

        Optional<Product> foundProduct2 = productRepo.findByNameIncludingDeleted("VPS");
        Assertions.assertTrue(foundProduct2.isPresent());
        Assertions.assertEquals("VPS", foundProduct2.get().getName());
        Assertions.assertFalse(foundProduct2.get().getIsDeleted());

        Optional<Product> foundProduct3 = productRepo.findByNameIncludingDeleted("Nonexistent Product");
        Assertions.assertFalse(foundProduct3.isPresent());

    }

    @Test
    @DisplayName("Test Find By Name Not Found")
    void testFindByNameNotFound() {
        Optional<Product> foundProduct = productRepo.findByNameIncludingDeleted("Nonexistent Product");
        Assertions.assertFalse(foundProduct.isPresent());
    }

    @Test
    @DisplayName("Test Find By Filter with keyword")
    void testFindByFilter() {
        Product product1 = createProduct("Basic Shared Hosting",
                productType, 10.0, 1, "10GB",
                "1000 Mbps", "1", "1", "1");
        entityManager.persist(product1);
        entityManager.flush();

        Product product2 = createProduct("VPS",
                productType, 100.0, 12, "100GB",
                "10000 Mbps", "5", "5", "5");
        entityManager.persist(product2);
        entityManager.flush();
        entityManager.clear();

        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> result = productRepo.findByFilter("Shared", pageable);
        Assertions.assertEquals(1, result.getTotalElements());
        Assertions.assertTrue(result.stream().anyMatch(
                product -> product.getName().equals("Basic Shared Hosting")));
        Assertions.assertFalse(result.stream().anyMatch(BaseEntity::getIsDeleted));
    }
}
