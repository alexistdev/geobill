package com.alexistdev.geobill.service;

import com.alexistdev.geobill.dto.CurrencyDTO;
import com.alexistdev.geobill.exceptions.DuplicateException;
import com.alexistdev.geobill.models.entity.Currency;
import com.alexistdev.geobill.models.repository.CurrencyRepo;
import com.alexistdev.geobill.request.CurrencyRequest;
import com.alexistdev.geobill.services.CurrencyService;
import com.alexistdev.geobill.utils.MessagesUtils;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CurrencyServiceTest {

    @Mock
    private CurrencyRepo currencyRepo;

    @Mock
    private MessagesUtils messagesUtils;

    @InjectMocks
    private CurrencyService currencyService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private CurrencyRequest buildRequest(String name, String code, String symbol, Double rate, boolean isDefault) {
        CurrencyRequest request = new CurrencyRequest();
        request.setName(name);
        request.setCode(code);
        request.setSymbol(symbol);
        request.setExchangeRate(rate);
        request.setDefault(isDefault);
        return request;
    }

    private Currency buildCurrency(String name, String code, String symbol, Double rate, boolean deleted) {
        Currency currency = new Currency();
        currency.setId(UUID.randomUUID());
        currency.setName(name);
        currency.setCode(code);
        currency.setSymbol(symbol);
        currency.setExchangeRate(rate);
        currency.setDeleted(deleted);
        currency.setIsDefault(false);
        return currency;
    }

    @Test
    @Order(1)
    @DisplayName("1. addCurrency - success, returns correct DTO")
    void addCurrency_success() {
        CurrencyRequest request = buildRequest("usd", "usd", "$", 1.0, false);

        Currency saved = buildCurrency("USD", "USD", "$", 1.0, false);

        when(currencyRepo.findByName("USD")).thenReturn(Optional.empty());
        when(currencyRepo.findByCode("USD")).thenReturn(Optional.empty());
        when(currencyRepo.save(any(Currency.class))).thenReturn(saved);

        CurrencyDTO result = currencyService.addCurrency(request);

        assertNotNull(result);
        assertEquals("USD", result.getName());
        assertEquals("USD", result.getCode());
        assertEquals("$", result.getSymbol());
        assertEquals(1.0, result.getExchangeRate());
    }

    @Test
    @Order(2)
    @DisplayName("2. addCurrency - name and code are uppercased before save")
    void addCurrency_uppercasesNameAndCode() {
        CurrencyRequest request = buildRequest("usd", "usd", "$", 1.0, false);

        Currency saved = buildCurrency("USD", "USD", "$", 1.0, false);

        when(currencyRepo.findByName("USD")).thenReturn(Optional.empty());
        when(currencyRepo.findByCode("USD")).thenReturn(Optional.empty());
        when(currencyRepo.save(any(Currency.class))).thenAnswer(inv -> {
            Currency c = inv.getArgument(0);
            assertEquals("USD", c.getName());
            assertEquals("USD", c.getCode());
            return saved;
        });

        currencyService.addCurrency(request);

        verify(currencyRepo).findByName("USD");
        verify(currencyRepo).findByCode("USD");
    }

    @Test
    @Order(3)
    @DisplayName("3. addCurrency - duplicate name (not deleted) throws DuplicateException")
    void addCurrency_duplicateName_throwsDuplicateException() {
        CurrencyRequest request = buildRequest("usd", "usd", "$", 1.0, false);

        Currency existing = buildCurrency("USD", "USD", "$", 1.0, false);

        when(currencyRepo.findByName("USD")).thenReturn(Optional.of(existing));
        when(messagesUtils.getMessage("currencyservice.currency_name_exist", "USD"))
                .thenReturn("Currency name USD already exist");

        DuplicateException ex = assertThrows(DuplicateException.class,
                () -> currencyService.addCurrency(request));

        assertEquals("Currency name USD already exist", ex.getMessage());
        verify(currencyRepo, never()).save(any());
    }

    @Test
    @Order(4)
    @DisplayName("4. addCurrency - duplicate name but deleted, proceeds normally")
    void addCurrency_duplicateNameButDeleted_success() {
        CurrencyRequest request = buildRequest("usd", "usd", "$", 1.0, false);

        Currency deletedCurrency = buildCurrency("USD", "USD", "$", 1.0, true);
        Currency saved = buildCurrency("USD", "USD", "$", 1.0, false);

        when(currencyRepo.findByName("USD")).thenReturn(Optional.of(deletedCurrency));
        when(currencyRepo.findByCode("USD")).thenReturn(Optional.empty());
        when(currencyRepo.save(any(Currency.class))).thenReturn(saved);

        CurrencyDTO result = currencyService.addCurrency(request);

        assertNotNull(result);
        assertEquals("USD", result.getName());
        verify(currencyRepo).save(any(Currency.class));
    }

    @Test
    @Order(5)
    @DisplayName("5. addCurrency - duplicate code (not deleted) throws DuplicateException")
    void addCurrency_duplicateCode_throwsDuplicateException() {
        CurrencyRequest request = buildRequest("euro", "eur", "€", 1.1, false);

        Currency existing = buildCurrency("EURO", "EUR", "€", 1.1, false);

        when(currencyRepo.findByName("EURO")).thenReturn(Optional.empty());
        when(currencyRepo.findByCode("EUR")).thenReturn(Optional.of(existing));
        when(messagesUtils.getMessage("currencyservice.currency_code_exist", "EUR"))
                .thenReturn("Currency code EUR already exist");

        DuplicateException ex = assertThrows(DuplicateException.class,
                () -> currencyService.addCurrency(request));

        assertEquals("Currency code EUR already exist", ex.getMessage());
        verify(currencyRepo, never()).save(any());
    }

    @Test
    @Order(6)
    @DisplayName("6. addCurrency - duplicate code but deleted, proceeds normally")
    void addCurrency_duplicateCodeButDeleted_success() {
        CurrencyRequest request = buildRequest("euro", "eur", "€", 1.1, false);

        Currency deletedCurrency = buildCurrency("EUR", "EUR", "€", 1.1, true);
        Currency saved = buildCurrency("EURO", "EUR", "€", 1.1, false);

        when(currencyRepo.findByName("EURO")).thenReturn(Optional.empty());
        when(currencyRepo.findByCode("EUR")).thenReturn(Optional.of(deletedCurrency));
        when(currencyRepo.save(any(Currency.class))).thenReturn(saved);

        CurrencyDTO result = currencyService.addCurrency(request);

        assertNotNull(result);
        verify(currencyRepo).save(any(Currency.class));
    }

    @Test
    @Order(7)
    @DisplayName("7. addCurrency - isDefault true, existing default with different code gets unset")
    void addCurrency_isDefault_unsetsExistingDefault() {
        CurrencyRequest request = buildRequest("euro", "eur", "€", 1.1, true);

        Currency existingDefault = buildCurrency("USD", "USD", "$", 1.0, false);
        existingDefault.setIsDefault(true);

        Currency saved = buildCurrency("EURO", "EUR", "€", 1.1, false);
        saved.setIsDefault(true);

        when(currencyRepo.findByName("EURO")).thenReturn(Optional.empty());
        when(currencyRepo.findByCode("EUR")).thenReturn(Optional.empty());
        when(currencyRepo.findByIsDefaultTrue()).thenReturn(Optional.of(existingDefault));
        when(currencyRepo.save(any(Currency.class))).thenReturn(saved);

        currencyService.addCurrency(request);

        assertFalse(existingDefault.getIsDefault());
    }

    @Test
    @Order(8)
    @DisplayName("8. addCurrency - isDefault true, same code as existing default does not unset it")
    void addCurrency_isDefault_sameCodeDoesNotUnsetExistingDefault() {
        CurrencyRequest request = buildRequest("usd", "usd", "$", 1.0, true);

        Currency existingDefault = buildCurrency("USD", "USD", "$", 1.0, false);
        existingDefault.setIsDefault(true);

        Currency saved = buildCurrency("USD", "USD", "$", 1.0, false);
        saved.setIsDefault(true);

        when(currencyRepo.findByName("USD")).thenReturn(Optional.empty());
        when(currencyRepo.findByCode("USD")).thenReturn(Optional.empty());
        when(currencyRepo.findByIsDefaultTrue()).thenReturn(Optional.of(existingDefault));
        when(currencyRepo.save(any(Currency.class))).thenReturn(saved);

        currencyService.addCurrency(request);

        assertTrue(existingDefault.getIsDefault());
    }

    @Test
    @Order(9)
    @DisplayName("9. addCurrency - isDefault false, existing default is not touched")
    void addCurrency_notDefault_doesNotTouchExistingDefault() {
        CurrencyRequest request = buildRequest("euro", "eur", "€", 1.1, false);

        Currency saved = buildCurrency("EURO", "EUR", "€", 1.1, false);

        when(currencyRepo.findByName("EURO")).thenReturn(Optional.empty());
        when(currencyRepo.findByCode("EUR")).thenReturn(Optional.empty());
        when(currencyRepo.save(any(Currency.class))).thenReturn(saved);

        currencyService.addCurrency(request);

        verify(currencyRepo, never()).findByIsDefaultTrue();
    }
}
