public enum FoodCategory
{
	COFFEE, SOUVLAKIA, PIZZA, BURGERS, PASTRY;

	@Override
	public String toString()
	{
		return this.name().charAt(0) + this.name().substring(1).toLowerCase();
	}
}
