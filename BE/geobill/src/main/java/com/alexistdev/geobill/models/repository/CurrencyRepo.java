package com.alexistdev.geobill.models.repository;

import com.alexistdev.geobill.models.entity.Currency;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CurrencyRepo extends JpaRepository<Currency, UUID> {

}

