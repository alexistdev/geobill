package com.alexistdev.geobill.models.repository;

import com.alexistdev.geobill.models.entity.ProductType;
import com.alexistdev.geobill.models.entity.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;

@DataJpaTest
@ActiveProfiles("test")
public class ProductTypeRepoTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ProductTypeRepo productTypeRepo;

    private static final String SYSTEM_USER = "System";

    @BeforeEach
    void setUp() {
        User testUser = new User();
        testUser.setEmail("testUser@gmail.com");
        testUser.setPassword("password");
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(testUser,null,new ArrayList<>());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private ProductType createProductType(String name) {
        ProductType productType = new ProductType();
        productType.setName(name);
        productType.setCreatedBy(SYSTEM_USER);
        productType.setCreatedDate(new java.util.Date());
        productType.setModifiedBy(SYSTEM_USER);
        productType.setModifiedDate(new java.util.Date());
        productType.setIsDeleted(false);
        return productType;
    }

    @Test
    @DisplayName("Test Save ProductType")
    void testSaveProductType() {
        ProductType productType = createProductType("VPS");
        ProductType savedProductType = productTypeRepo.save(productType);

        Assertions.assertNotNull(savedProductType);
        Assertions.assertEquals(productType.getName(), savedProductType.getName());
        Assertions.assertNotNull(savedProductType.getCreatedDate());
        Assertions.assertEquals(productType.getModifiedBy(), savedProductType.getModifiedBy());
        Assertions.assertNotNull(savedProductType.getModifiedDate());
        Assertions.assertFalse(savedProductType.getIsDeleted());
    }

    @Test
    @DisplayName("Test Find By ProductType UUID")
    void testFindByProductTypeUUID(){
        ProductType productType = createProductType("VPS");
        entityManager.persist(productType);
        entityManager.flush();
        ProductType foundProductType = productTypeRepo.findById(productType.getId()).orElse(null);
        Assertions.assertNotNull(foundProductType);
        Assertions.assertEquals(productType.getName(), foundProductType.getName());
    }

    @Test
    @DisplayName("Test Find All ProductType")
    void testFindAllProductType(){
        ProductType productType1 = createProductType("VPS");
        ProductType productType2 = createProductType("Dedicated Server");
        entityManager.persist(productType1);
        entityManager.persist(productType2);
        entityManager.flush();
        List<ProductType> foundProductTypes = productTypeRepo.findAll();
        Assertions.assertNotNull(foundProductTypes);
        Assertions.assertEquals(2, foundProductTypes.size());
    }

    @Test
    @DisplayName("Test Delete ProductType")
    void testDeleteProductType(){
        ProductType productType = createProductType("VPS");
        entityManager.persist(productType);
        entityManager.flush();

        productTypeRepo.delete(productType);
        entityManager.flush();
        entityManager.clear();

        List<ProductType> allProductTypes = productTypeRepo.findAll();
        Assertions.assertEquals(0, allProductTypes.size());

        //validate soft deleted
        Object isDeleted = entityManager.getEntityManager()
                .createNativeQuery("SELECT is_deleted FROM tb_product_types WHERE uuid = ?1")
                .setParameter(1, productType.getId())
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
    @DisplayName("Test Delete All ProductTypes")
    void testDeleteAllProductTypes() {
        ProductType productType1 = createProductType("VPS");
        ProductType productType2 = createProductType("Dedicated Server");
        entityManager.persist(productType1);
        entityManager.persist(productType2);
        entityManager.flush();

        productTypeRepo.deleteAll(List.of(productType1, productType2));
        entityManager.flush();
        entityManager.clear();

        List<ProductType> allProductTypes = productTypeRepo.findAll();
        Assertions.assertEquals(0, allProductTypes.size());

        //validate soft deleted
        Object isDeleted = entityManager.getEntityManager()
                .createNativeQuery("SELECT is_deleted FROM tb_product_types WHERE uuid = ?1")
                .setParameter(1, productType1.getId())
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
}
