package com.alexistdev.geobill.models.repository;

import com.alexistdev.geobill.models.entity.Hosting;
import com.alexistdev.geobill.models.entity.Product;
import com.alexistdev.geobill.models.entity.ProductType;
import com.alexistdev.geobill.models.entity.Role;
import com.alexistdev.geobill.models.entity.User;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@DataJpaTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("test")
public class HostingRepoTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private HostingRepo hostingRepo;

    private static final String SYSTEM_USER = "System";

    private ProductType productType;
    private Product product;
    private User user;

    @BeforeEach
    void setUp() {
        User testUser = new User();
        testUser.setEmail("testUser@gmail.com");
        testUser.setPassword("password");
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(testUser, null,
                new ArrayList<>());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        productType = new ProductType();
        productType.setName("Shared Hosting");
        productType.setCreatedBy(SYSTEM_USER);
        productType.setCreatedDate(new Date());
        productType.setModifiedBy(SYSTEM_USER);
        productType.setModifiedDate(new Date());
        productType.setIsDeleted(false);
        entityManager.persist(productType);

        product = new Product();
        product.setName("Hosting 1");
        product.setProductType(productType);
        product.setPrice(100.0);
        product.setCycle(12);
        product.setCapacity("100GB");
        product.setBandwith("1000 Mbps");
        product.setAddon_domain("5");
        product.setDatabase_account("5");
        product.setFtp_account("5");
        product.setCreatedBy(SYSTEM_USER);
        product.setCreatedDate(new Date());
        product.setModifiedBy(SYSTEM_USER);
        product.setModifiedDate(new Date());
        product.setIsDeleted(false);
        entityManager.persist(product);

        user = new User();
        user.setFullName("Test User");
        user.setEmail("test@gmail.com");
        user.setPassword("password");
        user.setRole(Role.USER);
        user.setDeleted(false);
        entityManager.persist(user);

        entityManager.flush();
    }

    private Hosting createHosting(String name, String domain, Double price, int status) {
        Hosting hosting = new Hosting();
        hosting.setHostingCode(
                "GE-" + java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase());
        hosting.setUser(user);
        hosting.setProduct(product);
        hosting.setName(name);
        hosting.setDomain(domain);
        hosting.setPrice(price);
        hosting.setStartDate(new Date());
        hosting.setEndDate(new Date());
        hosting.setStatus(status);
        hosting.setCreatedBy(SYSTEM_USER);
        hosting.setCreatedDate(new Date());
        hosting.setModifiedBy(SYSTEM_USER);
        hosting.setModifiedDate(new Date());
        hosting.setIsDeleted(false);
        return hosting;
    }

    @Test
    @Order(1)
    @DisplayName("1. Test Save Hosting")
    void testSaveHosting() {
        Hosting hosting = createHosting("Hosting 1 Domain.com", "domain.com", 100.0, 0);

        Hosting savedHosting = hostingRepo.save(hosting);

        Assertions.assertNotNull(savedHosting);
        Assertions.assertNotNull(savedHosting.getId());
        Assertions.assertEquals(hosting.getName(), savedHosting.getName());
        Assertions.assertEquals(hosting.getDomain(), savedHosting.getDomain());
        Assertions.assertEquals(hosting.getPrice(), savedHosting.getPrice());
        Assertions.assertEquals(hosting.getStatus(), savedHosting.getStatus());
        Assertions.assertEquals(product.getId(), savedHosting.getProduct().getId());
        Assertions.assertEquals(user.getId(), savedHosting.getUser().getId());
    }

    @Test
    @Order(2)
    @DisplayName("2. Test Find By Hosting UUID")
    void testFindByHostingUUID() {
        Hosting hosting = createHosting("Hosting 1 Domain.com", "domain.com", 100.0, 0);
        entityManager.persist(hosting);
        entityManager.flush();

        Optional<Hosting> foundHosting = hostingRepo.findById(hosting.getId());

        Assertions.assertTrue(foundHosting.isPresent());
        Assertions.assertEquals(hosting.getName(), foundHosting.get().getName());
        Assertions.assertEquals(hosting.getDomain(), foundHosting.get().getDomain());
    }

    @Test
    @Order(3)
    @DisplayName("3. Test Find All Hostings")
    void testFindAllHostings() {
        Hosting hosting1 = createHosting("Hosting 1 Domain.com", "domain.com", 100.0, 0);
        Hosting hosting2 = createHosting("Hosting 2 Domain.net", "domain.net", 200.0, 1);

        entityManager.persist(hosting1);
        entityManager.persist(hosting2);
        entityManager.flush();

        List<Hosting> hostings = hostingRepo.findAll();

        Assertions.assertEquals(2, hostings.size());
    }

    @Test
    @Order(4)
    @DisplayName("4. Test Delete Hosting")
    void testDeleteHosting() {
        Hosting hosting = createHosting("Hosting 1 Domain.com", "domain.com", 100.0, 0);
        entityManager.persist(hosting);
        entityManager.flush();

        hostingRepo.delete(hosting);
        entityManager.flush();
        entityManager.clear();

        Optional<Hosting> deletedHosting = hostingRepo.findById(hosting.getId());
        Assertions.assertFalse(deletedHosting.isPresent());

        Object isDeleted = entityManager.getEntityManager()
                .createNativeQuery("SELECT is_deleted FROM tb_hostings WHERE uuid = ?1")
                .setParameter(1, hosting.getId())
                .getSingleResult();

        boolean isDeletedBool = false;
        if (isDeleted instanceof Boolean) {
            isDeletedBool = (Boolean) isDeleted;
        } else if (isDeleted instanceof Number) {
            isDeletedBool = ((Number) isDeleted).intValue() == 1;
        }

        Assertions.assertTrue(isDeletedBool, "Hosting should be soft-deleted in the database");
    }
}
