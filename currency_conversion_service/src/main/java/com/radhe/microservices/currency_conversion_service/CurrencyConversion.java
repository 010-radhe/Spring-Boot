package com.radhe.microservices.currency_conversion_service;

import java.math.BigDecimal;

public class CurrencyConversion {
    private String from;
    private String to;
    private BigDecimal quantity;
    private BigDecimal conversionMultiple;
    private BigDecimal totalAmount;
    private String environment;

    //constructor
    public CurrencyConversion(String from, String to, BigDecimal quantity, BigDecimal conversionMultiple, BigDecimal totalAmount, String environment) {
        this.from = from;
        this.to = to;
        this.quantity = quantity;
        this.conversionMultiple = conversionMultiple;
        this.totalAmount = totalAmount;
        this.environment = environment;
    }

    //getters and setters
    public String getFrom() {
        return from;
    }
    public String getTo() {
        return to;
    }
    public BigDecimal getQuantity() {
        return quantity;
    }
    public BigDecimal getConversionMultiple() {
        return conversionMultiple;
    }
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
    public String getEnvironment() {
        return environment;
    }
    public void setFrom(String from) {
        this.from = from;
    }
    public void setTo(String to) {
        this.to = to;
    }
    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }
    public void setConversionMultiple(BigDecimal conversionMultiple) {
        this.conversionMultiple = conversionMultiple;
    }
    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
    public void setEnvironment(String environment) {
        this.environment = environment;
    }
}
