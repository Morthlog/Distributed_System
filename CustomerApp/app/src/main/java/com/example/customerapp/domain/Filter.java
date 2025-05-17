package com.example.customerapp.domain;

import java.io.Serial;
import java.io.Serializable;

public class Filter implements Serializable
{
    @Serial
    private static final long serialVersionUID = 1L;

    double latitude;
    double longitude;
    private int stars;
    private String [] priceCategories;
    private FoodCategory[] foodCategories;
    String[] availablePrices = {"$", "$$", "$$$"};
    int[] availableStars = {1, 2, 3, 4, 5};

    public FoodCategory[] getAvailableFoodCategories()
    {
        return FoodCategory.values();
    }

    public String[] getAvailablePrices()
    {
        return availablePrices.clone();
    }

    public int[] getAvailableStars()
    {
        return availableStars.clone();
    }

    public void setStars(int stars)
    {
        this.stars = stars;
    }

    public void setPriceCategories(String[] priceCategories)
    {
        this.priceCategories = priceCategories;
    }

    public void setFoodCategories(FoodCategory[] foodCategories)
    {
        this.foodCategories = foodCategories;
    }

    public int getStars()
    {
        return stars;
    }

    public String[] getPriceCategories()
    {
        return priceCategories.clone();
    }

    public FoodCategory[] getFoodCategories()
    {
        return foodCategories.clone();
    }


    public void setLatitude(double latitude)
    {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude)
    {
        this.longitude = longitude;
    }

    public double getLatitude()
    {
        return latitude;
    }

    public double getLongitude()
    {
        return longitude;
    }
}

