package com.example.customerapp;


import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class Store implements Serializable, StoreNameProvider{
    protected String storeName;
    protected double latitude;
    protected double longitude;
    protected String foodCategory;
    protected float stars;
    protected int noOfVotes;
    protected String storeLogo;
    protected String priceCategory;
    private final SerializableLock priceCategoryLock = new SerializableLock();

    protected final Map<String, Product> visibleProducts;

    public Store(String storeName, double latitude, double longitude, String foodCategory,
                 float stars, int noOfVotes, String storeLogo) {
        this.storeName = storeName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.foodCategory = foodCategory;
        this.stars = stars;
        this.noOfVotes = noOfVotes;
        this.storeLogo = storeLogo;

        this.visibleProducts = new HashMap<>();
        calculatePriceCategory();
    }

    public Store(JSONObject jsonObject) {
        this.storeName = (String) jsonObject.get("StoreName");
        this.latitude = ((Number) jsonObject.get("Latitude")).doubleValue();
        this.longitude = ((Number) jsonObject.get("Longitude")).doubleValue();
        this.foodCategory = (String) jsonObject.get("FoodCategory");
        this.stars = ((Number) jsonObject.get("Stars")).floatValue();
        this.noOfVotes = ((Number) jsonObject.get("NoOfVotes")).intValue();
        this.storeLogo = (String) jsonObject.get("StoreLogo");

        this.visibleProducts = new HashMap<>();

        JSONArray productList = (JSONArray) jsonObject.get("Products");

        if (productList != null) {
            for (Object productObj : productList) {
                JSONObject productJson = (JSONObject) productObj;

                String productName = (String) productJson.get("ProductName");
                ProductType productType =ProductType.fromString((String) productJson.get("ProductType")) ;
                int availableAmount = ((Number) productJson.get("Available Amount")).intValue();
                double price = ((Number) productJson.get("Price")).doubleValue();

                Product product = new Product(productName, productType, availableAmount, price);

                if (!product.isHidden()) {
                    visibleProducts.put(productName, product);
                }
            }
        }
        calculatePriceCategory();
    }

    protected void calculatePriceCategory() {
        double sum = 0.0;
        double averagePrice;
        synchronized (visibleProducts)
        {
            for (Product p : visibleProducts.values()) {
                sum += p.getPrice();
            }
            averagePrice = sum / visibleProducts.size();
        }
        synchronized (priceCategoryLock){
            if (averagePrice <= 5) {
                this.priceCategory = "$";
            } else if (averagePrice <= 15) {
                this.priceCategory = "$$";
            } else {
                this.priceCategory = "$$$";
            }
        }

    }

    public String getStoreName() {
        return storeName;
    }
    
    public Map<String, Product> getProducts() {
        synchronized (visibleProducts) {
            return new HashMap<>(visibleProducts);
        }
    }
    public String getFoodCategory()
    {
        return foodCategory;
    }

    public  String getPriceCategory () {
        synchronized (priceCategoryLock) {
            return priceCategory;
        }
    }

    public float getStars()
    {
        return stars;
    }

    public double getLatitude()
    {
        return latitude;
    }

    public double getLongitude()
    {
        return longitude;
    }

    public synchronized float addRating(RatingChange ratingChange)
    {
        float totalStars = stars * noOfVotes;
        int oldRating = ratingChange.getOldRating();
        if(oldRating!=0)
        {
            totalStars-=oldRating;
            noOfVotes--;
        }

        totalStars += ratingChange.getNewRating();
        noOfVotes ++;

        stars= totalStars/noOfVotes;
        return stars;
    }

    private class SerializableLock implements Serializable{
        SerializableLock() {}
    }
}
