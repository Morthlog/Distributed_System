package lib.shared;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Filter implements Serializable
{
    @Serial
    private static final long serialVersionUID = 1L;

    double latitude;
    double longitude;
    private int stars;
    private List<String> priceCategories;
    private List<FoodCategory> foodCategories;
    String[] availablePrices = {"$", "$$", "$$$"};
    int[] availableStars = {1, 2, 3, 4, 5};

    public  List<FoodCategory> getAvailableFoodCategories()
    {
        return List.of(FoodCategory.values());
    }

    public List<String> getAvailablePrices()
    {
        return new ArrayList<>(List.of(availablePrices));
    }

    public int[] getAvailableStars()
    {
        return availableStars.clone();
    }

    public void setStars(int stars)
    {
        this.stars = stars;
    }

    public void setPriceCategories(List<String> priceCategories)
    {
        this.priceCategories = priceCategories;
    }

    public void setFoodCategories(List<FoodCategory> foodCategories)
    {
        this.foodCategories = foodCategories;
    }

    public int getStars()
    {
        return stars;
    }

    public List<String> getPriceCategories()
    {
        return new ArrayList<> (priceCategories);
    }

    public List<FoodCategory>  getFoodCategories()
    {
        return new ArrayList<> (foodCategories);
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

