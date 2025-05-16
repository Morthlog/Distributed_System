package com.example.customerapp;

public enum ProductType
{
    CHICKEN_BURGER,
    BEEF_BURGER,
    FRIES,
    VEGETARIAN_BURGER,
    PIE,
    SWEETS,
    FRAPPE,
    AMERICANO,
    ESPRESSO,
    LATTE,
    PIZZA,
    CALZONE,
    PITA_BREAD,
    PIECE;

    @Override
    public String toString()
    {
        return this.name().replace('_', ' ').toLowerCase();
    }

    public static ProductType fromString(String str)
    {
        return ProductType.valueOf(str.toUpperCase().replace(' ', '_'));
    }
}
