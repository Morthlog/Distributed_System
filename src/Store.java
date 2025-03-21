import java.io.Serializable;
import java.util.List;

public class Store implements Serializable
{
    private String storeName;
    private double latitude;
    private double longitude;
    private FoodCategory foodCategory;
    private int stars;
    private int noOfVotes;
    private String storeLogo;
    private List<Product> products;

    public Store(String storeName, FoodCategory foodCategory, int stars)
    {
        this.storeName = storeName;
        this.foodCategory = foodCategory;
        this.stars = stars;
    }

    public String getStoreName()
    {
        return storeName;
    }

    public FoodCategory getFoodCategory()
    {
        return foodCategory;
    }

    public int getStars()
    {
        return stars;
    }


    public List<Product> getProducts()
    {
        return products;
    }

    public void setProducts(List<Product> products)
    {
        this.products=products;
    }
}
