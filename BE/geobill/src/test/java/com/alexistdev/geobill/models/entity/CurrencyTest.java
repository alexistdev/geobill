package com.alexistdev.geobill.models.entity;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.*;

import java.lang.reflect.Field;
import java.util.Set;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CurrencyTest {

    private static Validator validator;
    private Currency currency;
    private String name;
    private String symbol;
    private String code;
    private Double exchangeRate;
    private boolean isDefault;

    @BeforeAll
    static void beforeAll() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @BeforeEach
    void setUp() {
        name = "US Dollar";
        symbol = "$";
        code = "USD";
        exchangeRate = 1.0;
        isDefault = true;

        currency = new Currency();
        currency.setName(name);
        currency.setSymbol(symbol);
        currency.setCode(code);
        currency.setExchangeRate(exchangeRate);
        currency.setIsDefault(isDefault);
    }

    @Test
    @Order(1)
    @DisplayName("1. Test Getter")
    void testGetter() {
        Assertions.assertEquals(name, currency.getName());
        Assertions.assertEquals(symbol, currency.getSymbol());
        Assertions.assertEquals(code, currency.getCode());
        Assertions.assertEquals(exchangeRate, currency.getExchangeRate());
        Assertions.assertEquals(isDefault, currency.getIsDefault());
    }

    @Test
    @Order(2)
    @DisplayName("2. Test Setter")
    void testSetter() {
        String newName = "Rupiah";
        String newSymbol = "Rp";
        String newCode = "IDR";
        Double newExchangeRate = 1000.0;
        boolean newIsDefault = false;

        currency.setName(newName);
        currency.setSymbol(newSymbol);
        currency.setCode(newCode);
        currency.setExchangeRate(newExchangeRate);
        currency.setIsDefault(newIsDefault);

        Assertions.assertEquals(newName, currency.getName());
        Assertions.assertEquals(newSymbol, currency.getSymbol());
        Assertions.assertEquals(newCode, currency.getCode());
        Assertions.assertEquals(newExchangeRate, currency.getExchangeRate());
        Assertions.assertEquals(newIsDefault, currency.getIsDefault());
    }

    @Test
    @Order(3)
    @DisplayName("3. Test IsDefault")
    void testIsDefault() {
        currency.setIsDefault(true);
        Assertions.assertTrue(currency.getIsDefault());
    }

    @Test
    @Order(4)
    @DisplayName("4. Test Currency Entity")
    void testCurrencyEntity() {
        Assertions.assertInstanceOf(Currency.class, new Currency());
    }

    @Test
    @Order(5)
    @DisplayName("5. Test Currency Entity Validation")
    void testCurrencyEntityValidation() {
        Set<ConstraintViolation<Currency>> violations = validator.validate(currency);
        Assertions.assertTrue(violations.isEmpty());
    }

    @Test
    @Order(6)
    @DisplayName("6. Test Name Must Not Be Blank")
    void testNameMustNotBeBlank() {
        currency.setName("   ");
        assertViolationForField("name");
    }

    @Test
    @Order(7)
    @DisplayName("7. Test Name Max Length")
    void testNameMaxLength() {
        currency.setName("123456789012345678901234567890123456789012345678901");
        assertViolationForField("name");
    }

    @Test
    @Order(8)
    @DisplayName("8. Test Code Must Not Be Blank")
    void testCodeMustNotBeBlank() {
        currency.setCode(" ");
        assertViolationForField("code");
    }

    @Test
    @Order(9)
    @DisplayName("9. Test Code Max Length")
    void testCodeMaxLength() {
        currency.setCode("123456");
        assertViolationForField("code");
    }

    @Test
    @Order(10)
    @DisplayName("10. Test Symbol Must Not Be Blank")
    void testSymbolMustNotBeBlank() {
        currency.setSymbol(" ");
        assertViolationForField("symbol");
    }

    @Test
    @Order(11)
    @DisplayName("11. Test Symbol Max Length")
    void testSymbolMaxLength() {
        currency.setSymbol("12345678901");
        assertViolationForField("symbol");
    }

    @Test
    @Order(12)
    @DisplayName("12. Test Exchange Rate Must Not Be Null")
    void testExchangeRateMustNotBeNull() {
        currency.setExchangeRate(null);
        assertViolationForField("exchangeRate");
    }

    @Test
    @Order(13)
    @DisplayName("13. Test Exchange Rate Must Be Positive")
    void testExchangeRateMustBePositive() {
        currency.setExchangeRate(0.0);
        assertViolationForField("exchangeRate");
    }

    @Test
    @Order(14)
    @DisplayName("14. Test IsDefault Must Not Be Null")
    void testIsDefaultMustNotBeNull() {
        currency.setIsDefault(null);
        assertViolationForField("isDefault");
    }

    @Test
    @Order(15)
    @DisplayName("15. Test ID is UUID")
    void testIdIsUUID() throws NoSuchFieldException {
        Field field = Currency.class.getDeclaredField("id");
        Assertions.assertEquals(java.util.UUID.class, field.getType());
    }

    private void assertViolationForField(String fieldName) {
        Set<ConstraintViolation<Currency>> violations = validator.validate(currency);
        Assertions.assertTrue(
                violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals(fieldName)),
                "Expected validation violation for field: " + fieldName
        );
    }
}
