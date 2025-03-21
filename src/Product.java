import java.io.Serializable;
import java.math.BigDecimal;

public class Product implements Serializable
{
    private String name;
    private BigDecimal price;

    public Product(String name, BigDecimal price)
    {
        this.name=name;
        this.price=price;
    }

    public String getName()
    {
        return  name;
    }

    public BigDecimal getPrice()
    {
        return  price;
    }
}
