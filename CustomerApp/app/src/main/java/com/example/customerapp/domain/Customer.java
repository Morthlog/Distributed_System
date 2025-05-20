package com.example.customerapp.domain;
import java.util.HashMap;
import java.util.Map;

import lib.shared.ShoppingCart;

public class Customer
{
    private final ShoppingCart shoppingCart = new ShoppingCart();
    private final Map<String, Integer> storeRatings = new HashMap<>();
    String name;
    public Customer(String name)
    {
        this.name = name;
    }

    public void addToCart(String productName, int quantity)
    {
        shoppingCart.addProduct(productName, quantity);
    }

    public void setStoreForCart(String storeName)
    {
        shoppingCart.setStoreName(storeName);
    }

    public void clearShoppingCart()
    {
        shoppingCart.clear();
    }

    public ShoppingCart getShoppingCart()
    {
        return shoppingCart;
    }

    public void saveRating(String storeName, int rating)
    {
        storeRatings.put(storeName, rating);
    }

    public int getOldRating(String storeName)
    {
        return storeRatings.getOrDefault(storeName, 0);
    }

    public String getName()
    {
        return name;
    }
}
