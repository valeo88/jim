package ru.valeo.jim.service.impl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class DictionariesServiceImplTest {

    @Autowired
    private DictionariesServiceImpl service;

    @Test
    void shouldHaveSomeCurrencies() {
        assertFalse(service.getCurrencies().isEmpty());
    }

    @Test
    void shouldSaveNewCurrency() {
        var newCurrency = service.saveCurrency("XXX", "New currency", "000");

        var currencies = service.getCurrencies();
        assertTrue(currencies.contains(newCurrency));
    }

    @Test
    void shouldUpdateExistingCurrency() {
        var currencyCode = service.getCurrencies().get(0).getCode();

        var updated = service.saveCurrency(currencyCode, "New currency", "000");

        var currencies = service.getCurrencies();
        assertTrue(currencies.contains(updated));
    }

    @Test
    void shouldDeleteCurrencyIfExists() {
        String code = "XXX";
        var newCurrency = service.saveCurrency("XXX", "New currency", "000");

        service.deleteCurrency(code);

        var currencies = service.getCurrencies();
        assertFalse(currencies.contains(newCurrency));
    }

    @Test
    void shouldHaveSomeInstrumentCategories() {
        assertFalse(service.getInstrumentCategories().isEmpty());
    }

    @Test
    void shouldSaveNewInstrumentCategory() {
        var newCategory = service.saveInstrumentCategory("XXX", "New category");

        var categories = service.getInstrumentCategories();
        assertTrue(categories.contains(newCategory));
    }

    @Test
    void shouldUpdateExistingInstrumentCategory() {
        var code = service.getInstrumentCategories().get(0).getCode();

        var updated = service.saveInstrumentCategory(code, "New category");

        var categories = service.getInstrumentCategories();
        assertTrue(categories.contains(updated));
    }

    @Test
    void shouldDeleteInstrumentCategoryIfExists() {
        String code = "XXX";
        var newCategory = service.saveInstrumentCategory("XXX", "New currency");

        service.deleteInstrumentCategory(code);

        var categories = service.getInstrumentCategories();
        assertFalse(categories.contains(newCategory));
    }

    @Test
    void getInstrumentTypes() {
        assertFalse(service.getInstrumentTypes().isEmpty());
    }
}