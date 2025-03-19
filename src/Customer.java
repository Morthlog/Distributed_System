import java.util.Set;

public class Customer
{

	Filter filter;
	Set<String> cart;
	String name;
	
	public Customer(String name)
	{
		this.name = name;
	}

	public void setFilter(Filter filter)
	{
		this.filter = filter;
	}

	public Filter getFilter()
	{
		return filter;
	}

	public void search()
	{

	}

	public void buy()
	{

	}

}
