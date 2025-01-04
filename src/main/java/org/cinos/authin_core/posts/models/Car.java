package org.cinos.authin_core.posts.models;

import lombok.Builder;

@Builder
public class Car {
    private String make;
    private String model;
    private String year;
    private Double price;
    private CurrencySymbol currencySymbol;
    private String kilometer;
    private Boolean isUsed;
}
