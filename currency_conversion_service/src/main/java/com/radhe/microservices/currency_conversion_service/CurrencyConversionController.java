package com.radhe.microservices.currency_conversion_service;

import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

@RestController
public class CurrencyConversionController {
    private final Environment environment;
    public CurrencyConversionController(Environment environment) {
        this.environment = environment;
    }
    @GetMapping("currency-conversion/from/USD/to/INR/quantity/{quantity}")
    public CurrencyConversion getCurrencyConversion(@PathVariable BigDecimal quantity){
        //call currency exchange service to get the conversion multiple

        //create a new RestTemplate object
        RestTemplate restTemplate = new RestTemplate(); 

        //call the currency exchange service to get the conversion multiple
        ResponseEntity<CurrencyExchange> responseEntity = restTemplate.getForEntity("http://localhost:8000/currency-exchange/from/USD/to/INR", CurrencyExchange.class);
        CurrencyExchange currencyExchange = responseEntity.getBody();
        
        //set the conversion multiple
        BigDecimal conversionMultiple = currencyExchange.getConversionMultiple();
        
        //set the total amount
        BigDecimal totalAmount = quantity.multiply(conversionMultiple);
        CurrencyConversion currencyConversion = new CurrencyConversion("USD","INR",quantity,conversionMultiple,totalAmount,environment.getProperty("local.server.port"));
        return currencyConversion;
    }
}
