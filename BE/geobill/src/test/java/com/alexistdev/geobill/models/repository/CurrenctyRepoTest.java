package com.alexistdev.geobill.models.repository;

import com.alexistdev.geobill.models.entity.Currency;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

@DataJpaTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("test")
public class CurrenctyRepoTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CurrencyRepo currencyRepo;

    private static final String SYSTEM_USER = "System";

    private Currency currency;

    @BeforeEach
    void setUp() {
        currency = new Currency();
        currency.setName("US Dollar");
        currency.setSymbol("$");
        currency.setCode("USD");
        currency.setExchangeRate(1.0);
        currency.setIsDefault(true);
        currency.setCreatedBy(SYSTEM_USER);
        currency.setCreatedDate(new java.util.Date());
        currency.setModifiedBy(SYSTEM_USER);
        currency.setModifiedDate(new java.util.Date());
        currency.setIsDeleted(false);
        entityManager.persist(currency);

        entityManager.flush();
    }

    private Currency createCurrency(String name, String symbol, String code, double exchangeRate, boolean isDefault) {
        Currency createdCurrency = new Currency();
        createdCurrency.setName(name);
        createdCurrency.setSymbol(symbol);
        createdCurrency.setCode(code);
        createdCurrency.setExchangeRate(exchangeRate);
        createdCurrency.setIsDefault(isDefault);
        createdCurrency.setCreatedBy(SYSTEM_USER);
        createdCurrency.setCreatedDate(new java.util.Date());
        createdCurrency.setModifiedBy(SYSTEM_USER);
        createdCurrency.setModifiedDate(new java.util.Date());
        createdCurrency.setIsDeleted(false);
        return createdCurrency;
    }

    @Test
    @Order(1)
    @DisplayName("1. Test Save Currency")
    void testSaveCurrency() {
        Currency currency = createCurrency("Euro", "€", "EUR", 0.85, false);
        Currency savedCurrency = currencyRepo.save(currency);
        Assertions.assertNotNull(savedCurrency);
        Assertions.assertEquals(currency.getName(), savedCurrency.getName());
        Assertions.assertEquals(currency.getSymbol(), savedCurrency.getSymbol());
        Assertions.assertEquals(currency.getCode(), savedCurrency.getCode());
        Assertions.assertEquals(currency.getExchangeRate(), savedCurrency.getExchangeRate());
        Assertions.assertEquals(currency.getIsDefault(), savedCurrency.getIsDefault());
        Assertions.assertEquals(currency.getCreatedBy(), savedCurrency.getCreatedBy());
        Assertions.assertNotNull(savedCurrency.getCreatedDate());
        Assertions.assertEquals(currency.getModifiedBy(), savedCurrency.getModifiedBy());
        Assertions.assertNotNull(savedCurrency.getModifiedDate());
        Assertions.assertFalse(savedCurrency.getIsDeleted());
    }

    @Test
    @Order(2)
    @DisplayName("2. Test Find By Currency UUID")
    void testFindByCurrencyUUID() {
        Currency currency1 = createCurrency("British Pound", "£", "GBP", 0.75, false);
        entityManager.persist(currency1);
        entityManager.flush();

        Optional<Currency> foundCurrency = currencyRepo.findById(currency1.getId());

        Assertions.assertTrue(foundCurrency.isPresent());
        Assertions.assertEquals(currency1.getName(), foundCurrency.get().getName());
        Assertions.assertEquals(currency1.getSymbol(), foundCurrency.get().getSymbol());
        Assertions.assertEquals(currency1.getCode(), foundCurrency.get().getCode());
        Assertions.assertEquals(currency1.getExchangeRate(), foundCurrency.get().getExchangeRate());
        Assertions.assertEquals(currency1.getIsDefault(), foundCurrency.get().getIsDefault());
        Assertions.assertEquals(currency1.getCreatedBy(), foundCurrency.get().getCreatedBy());
        Assertions.assertNotNull(foundCurrency.get().getCreatedDate());
        Assertions.assertEquals(currency1.getModifiedBy(), foundCurrency.get().getModifiedBy());
        Assertions.assertNotNull(foundCurrency.get().getModifiedDate());
        Assertions.assertFalse(foundCurrency.get().getIsDeleted());
        Assertions.assertNotNull(foundCurrency.get().getId());
        Assertions.assertNotNull(foundCurrency.get().getCreatedDate());
        Assertions.assertNotNull(foundCurrency.get().getModifiedDate());
        Assertions.assertNotNull(foundCurrency.get().getCreatedBy());
        Assertions.assertNotNull(foundCurrency.get().getModifiedBy());
    }

    @Test
    @Order(3)
    @DisplayName("3. Test Find All Currencies")
    void testFindAllCurrencies() {
        java.util.List<Currency> currencies = currencyRepo.findAll();
        Assertions.assertEquals(1, currencies.size());
    }

    @Test
    @Order(4)
    @DisplayName("4. Test Delete Currency")
    void testDeleteCurrency() {
        currencyRepo.delete(currency);
        entityManager.flush();
        entityManager.clear();

        Optional<Currency> deletedCurrency = currencyRepo.findById(currency.getId());
        Assertions.assertFalse(deletedCurrency.isPresent());

        Object isDeleted = entityManager.getEntityManager()
                .createNativeQuery("SELECT is_deleted FROM tb_currencies WHERE uuid = ?1")
                .setParameter(1, currency.getId())
                .getSingleResult();

        boolean isDeletedBool = false;
        if (isDeleted instanceof Boolean) {
            isDeletedBool = (Boolean) isDeleted;
        } else if (isDeleted instanceof Number) {
            isDeletedBool = ((Number) isDeleted).intValue() == 1;
        }

        Assertions.assertTrue(isDeletedBool, "Currency should be soft-deleted in the database");
    }

    @Test
    @Order(5)
    @DisplayName("5. Test Delete All Currencies")
    void testDeleteAllCurrencies() {
        Currency currency2 = createCurrency("Japanese Yen", "¥", "JPY", 110.0, false);
        entityManager.persist(currency2);
        entityManager.flush();
        entityManager.clear();

        currencyRepo.deleteAll();
        entityManager.flush();
        entityManager.clear();

        List<Currency> allCurrencies = currencyRepo.findAll();
        Assertions.assertEquals(0, allCurrencies.size());

        List<?> results = entityManager.getEntityManager()
                .createNativeQuery("SELECT is_deleted FROM tb_currencies WHERE uuid IN (?1, ?2)")
                .setParameter(1, currency.getId())
                .setParameter(2, currency2.getId())
                .getResultList();

        Assertions.assertEquals(2, results.size(), "Both currencies should still exist in the DB");
        for (Object isDeleted : results) {
            boolean isDeletedBool = false;
            if (isDeleted instanceof Boolean) {
                isDeletedBool = (Boolean) isDeleted;
            } else if (isDeleted instanceof Number) {
                isDeletedBool = ((Number) isDeleted).intValue() == 1;
            }
            Assertions.assertTrue(isDeletedBool, "Currency should be soft-deleted in the database");
        }
    }
}
