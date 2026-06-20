package com.radhe.microservices.currency_conversion_service;

import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
public class CurrencyConversionFeignController {
    private final Environment environment;
    private final CurrencyExchangeProxy proxy;

    public CurrencyConversionFeignController(Environment environment, CurrencyExchangeProxy proxy) {
        this.environment = environment;
        this.proxy = proxy;
    }

    @GetMapping("currency-conversion/feign/from/USD/to/INR/quantity/{quantity}")
    public CurrencyConversion getCurrencyConversion(@PathVariable BigDecimal quantity){
        
        CurrencyExchange currencyExchange = proxy.getCurrencyExchange();
        BigDecimal conversionMultiple = currencyExchange.getConversionMultiple();
        BigDecimal totalAmount = quantity.multiply(conversionMultiple);
        return new CurrencyConversion(currencyExchange.getFrom(), currencyExchange.getTo(), quantity, conversionMultiple, totalAmount, currencyExchange.getEnvironment());
    }
}
