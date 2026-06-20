package com.radhe.microservices.currency_conversion_service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "currency-exchange-service")
public interface CurrencyExchangeProxy {
    @GetMapping("currency-exchange/from/USD/to/INR")
    public CurrencyExchange getCurrencyExchange();
}
