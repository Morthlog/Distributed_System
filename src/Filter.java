
import java.io.Serializable;

public class Filter implements Serializable
{
	private static final long serialVersionUID = 1L;

	double latitude;
	double longitude;
	private int stars;
	private String price;
	private FoodCategory [] categories;



	String[] availablePrices = { "$", "$$", "$$$" };

	int[] availableStars = { 1, 2, 3, 4, 5 };

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

	public void setPrice(String price)
	{
		this.price = price;
	}

	public void setCategories(FoodCategory[] categories)
	{
		this.categories = categories;
	}

	public int getStars()
	{
		return stars;
	}

	public String getPrice() {
		return price;
	}

	public FoodCategory[] getCategories() {
		return categories.clone();
	}

	@Override
	public String toString()
	{
		return "Filter [Stars=" + stars +
				", Price=" + price +
				", Categories=" + (categories != null ? java.util.Arrays.toString(categories) : "None") + "]";
	}

	public void setCoordinates(double latitude, double longitude)
	{
		this.latitude=latitude;
		this.longitude=longitude;
	}
}

