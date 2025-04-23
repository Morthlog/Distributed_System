import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ShoppingCart implements Serializable
{
    private String storeName;
    private Map<String,Integer> products = new HashMap<>();//product Name and count

    public ShoppingCart(){}

    public void setStoreName(String storeName)
    {
        this.storeName = storeName;
    }

    public String getStoreName()
    {
        return storeName;
    }

    public void addProduct(String name, int count)
    {
        products.put(name, count);
    }

    public Map<String, Integer> getProducts()
    {
        return new HashMap<>(products);
    }

    public void clear()
    {
        storeName="";
        products.clear();
    }
}
