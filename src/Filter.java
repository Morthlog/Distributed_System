
import java.util.Set;

public class Filter
{
	int stars;
	String price;
	Set<FoodCategory> categories;

	enum FoodCategory
	{
		COFFEE, SOUVLAKIA, PIZZA, BURGERS, PASTRY;

		@Override
		public String toString()
		{
			return this.name().charAt(0) + this.name().substring(1).toLowerCase();
		}
	}

	String[] availablePrices = { "$", "$$", "$$$" };

	int[] availableStars = { 1, 2, 3, 4, 5 };

	public String[] getAvailablePrices()
	{
		return availablePrices.clone();
	}

	public FoodCategory[] getAvailableFoodCategories()
	{
		return FoodCategory.values();
	}

	public int[] getAvailableStars()
	{
		return availableStars.clone();
	}

	public void setStars(int stars)
	{
		this.stars = stars;
	}

	public void setPrices(String price)
	{
		this.price = price;
	}

	public void setCategories(Set<FoodCategory> categories)
	{
		this.categories = categories;
	}

	public class Main
	{
		public static void main(String[] args)
		{
			FoodCategory category = FoodCategory.SOUVLAKIA;

			
			System.out.println(category.name()); //  SOUVLAKIA
			System.out.println(category.toString()); //Souvlakia
		}
	}
}
