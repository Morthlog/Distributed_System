package lib.shared;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ShoppingCart implements Serializable, StoreNameProvider
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
        int currentCount = 0;
        if (this.products.containsKey(name))
        {
            currentCount = this.products.get(name);
        }
        int newCount = currentCount + count;

        if (newCount > 0)
        {
            this.products.put(name, newCount);
        }
        else
        {
            this.products.remove(name);
        }
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
