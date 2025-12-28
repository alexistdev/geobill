package com.alexistdev.geobill.models.repository;

import com.alexistdev.geobill.models.entity.Domaintld;
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@DataJpaTest
@ActiveProfiles("test")
public class DomaintldRepoTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private DomaintldRepo domaintldRepo;

    private String domainTldName1;
    private String domainTldName2;

    @BeforeEach
    void setUp() {
        domainTldName1 = ".com";
        domainTldName2 = ".co.id";
        User testUser = new User();
        testUser.setEmail("testUser@gmail.com");
        testUser.setPassword("password");
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(testUser, null,
                new ArrayList<>());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private Domaintld createDomaintld(String name) {
        Domaintld domainTld = new Domaintld();
        domainTld.setName(name);
        domainTld.setCreatedBy("system");
        domainTld.setCreatedDate(new java.util.Date());
        domainTld.setIsDeleted(false);
        return domainTld;
    }

    @Test
    @DisplayName("Test Save Domaintld")
    void testSaveDomaintld() {
        Domaintld domainTld = createDomaintld(domainTldName1);
        Domaintld savedDomainTld = domaintldRepo.save(domainTld);

        Assertions.assertNotNull(savedDomainTld);
        Assertions.assertEquals(domainTldName1, savedDomainTld.getName());
        Assertions.assertEquals("testUser@gmail.com", savedDomainTld.getCreatedBy());
        Assertions.assertNotNull(savedDomainTld.getCreatedDate());
        Assertions.assertFalse(savedDomainTld.getIsDeleted());
    }

    @Test
    @DisplayName("Test Find By Domaintld UUID")
    void testFindByDomaintldUUID() {
        Domaintld domainTld = createDomaintld(domainTldName1);
        entityManager.persist(domainTld);
        entityManager.flush();
        Optional<Domaintld> foundDomainTld = domaintldRepo.findById(domainTld.getId());

        Assertions.assertTrue(foundDomainTld.isPresent(), "DomainTLD should be present in the database");
        Assertions.assertEquals(domainTldName1, foundDomainTld.get().getName());
    }

    @Test
    @DisplayName("Test Find All Domaintlds")
    void testFindAllDomaintlds() {
        Domaintld domainTld1 = createDomaintld(domainTldName1);
        entityManager.persist(domainTld1);

        Domaintld domainTld2 = createDomaintld(domainTldName2);
        entityManager.persist(domainTld2);

        List<Domaintld> allDomaintlds = domaintldRepo.findAll();
        Assertions.assertEquals(2, allDomaintlds.size());
    }

    @Test
    @DisplayName("Test Delete Domaintlds")
    void testDeleteDomaintlds() {
        Domaintld domainTld = createDomaintld(domainTldName1);
        entityManager.persist(domainTld);
        entityManager.flush();
        domaintldRepo.delete(domainTld);
        Optional<Domaintld> foundDomainTld = domaintldRepo.findById(domainTld.getId());
        Assertions.assertFalse(foundDomainTld.isPresent());
    }

    @Test
    @DisplayName("Test Delete All Domainstlds")
    void testDeleteAllDomainstlds() {
        Domaintld domainTld1 = createDomaintld(domainTldName1);
        entityManager.persist(domainTld1);

        Domaintld domainTld2 = createDomaintld(domainTldName2);
        entityManager.persist(domainTld2);

        domaintldRepo.deleteAll();

        List<Domaintld> allDomaintlds = domaintldRepo.findAll();
        Assertions.assertEquals(0, allDomaintlds.size());
        Assertions.assertTrue(allDomaintlds.isEmpty(), "All DomainTLDs should be deleted");
    }

    @Test
    @DisplayName("Test Find By Domaintld UUID Not Found")
    void testFindByDomaintldUUIDNotFound() {
        UUID nonExistendID = UUID.randomUUID();
        Optional<Domaintld> foundDomainTld = domaintldRepo.findById(nonExistendID);
        Assertions.assertFalse(foundDomainTld.isPresent());
    }

    @Test
    @DisplayName("Test Update Domaintld")
    void testUpdateDomaintld() {
        Domaintld domainTld = createDomaintld(domainTldName1);
        entityManager.persist(domainTld);
        entityManager.flush();

        domainTld.setName(".org");
        domaintldRepo.save(domainTld);
        entityManager.flush();

        Optional<Domaintld> foundDomainTld = domaintldRepo.findById(domainTld.getId());
        Assertions.assertTrue(foundDomainTld.isPresent());
        Assertions.assertEquals(".org", foundDomainTld.get().getName());
    }

    @Test
    @DisplayName("Test Serializable")
    void testSerializable() {
        Assertions.assertInstanceOf(Serializable.class, domaintldRepo);
    }

    @Test
    @DisplayName("Test DomainTld Entity")
    void testDomainTldEntity() {
        Assertions.assertInstanceOf(Domaintld.class, new Domaintld());
    }

    @Test
    @DisplayName("Test DomainTld Repo")
    void testDomainTldRepo() {
        Assertions.assertInstanceOf(DomaintldRepo.class, domaintldRepo);
    }

}
