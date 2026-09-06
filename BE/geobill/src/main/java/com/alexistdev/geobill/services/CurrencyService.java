package com.alexistdev.geobill.services;

import com.alexistdev.geobill.dto.CurrencyDTO;
import com.alexistdev.geobill.exceptions.DuplicateException;
import com.alexistdev.geobill.models.entity.Currency;
import com.alexistdev.geobill.models.repository.CurrencyRepo;
import com.alexistdev.geobill.request.CurrencyRequest;
import com.alexistdev.geobill.utils.MessagesUtils;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;

@Slf4j
@Service
public class CurrencyService {

    private final CurrencyRepo currencyRepo;
    private final MessagesUtils messagesUtils;

    public CurrencyService(CurrencyRepo currencyRepo, MessagesUtils messagesUtils) {
        this.currencyRepo = currencyRepo;
        this.messagesUtils = messagesUtils;
    }

    @Transactional
    public CurrencyDTO addCurrency(CurrencyRequest request){
        String name = request.getName().toUpperCase();
        String code = request.getCode().toUpperCase();

        currencyRepo.findByName(name)
                .filter(c-> c.getDeleted() == false)
                .ifPresent(c->{
            String message = messagesUtils.getMessage("currencyservice.currency_name_exist", name);
            log.info(message);
            throw new DuplicateException(message);
        });

        currencyRepo.findByCode(code)
                .filter(c-> c.getDeleted() == false)
                .ifPresent(c->{
           String message = messagesUtils.getMessage("currencyservice.currency_code_exist", code);
           log.info(message);
           throw new DuplicateException(message);
        });

        if(request.isDefault()){
            currencyRepo.findByIsDefaultTrue()
                    .filter(current-> !current.getCode().equals(code))
                    .ifPresent(current -> current.setIsDefault(false));
        }

        Currency savedCurrency = currencyRepo.save(createCurrency(request));
        return this.convertToDTO(savedCurrency);
    }

    private CurrencyDTO convertToDTO(Currency currency){
        CurrencyDTO currencyDTO = new CurrencyDTO();
        currencyDTO.setName(currency.getName());
        currencyDTO.setSymbol(currency.getSymbol());
        currencyDTO.setCode(currency.getCode());
        currencyDTO.setExchangeRate(currency.getExchangeRate());
        return currencyDTO;
    }

    private Currency createCurrency(CurrencyRequest request){
        String DEFAULT_CREATED_BY = "System";
        Date now = new Date();
        Currency currencyToSave = new Currency();
        currencyToSave.setName(request.getName().toUpperCase());
        currencyToSave.setSymbol(request.getSymbol());
        currencyToSave.setCode(request.getCode().toUpperCase());
        currencyToSave.setExchangeRate(request.getExchangeRate());
        currencyToSave.setDeleted(false);
        currencyToSave.setCreatedDate(now);
        currencyToSave.setModifiedDate(now);
        currencyToSave.setCreatedBy(DEFAULT_CREATED_BY);
        currencyToSave.setModifiedBy(DEFAULT_CREATED_BY);
        currencyToSave.setIsDefault(request.isDefault());
        return currencyToSave;
    }

}
