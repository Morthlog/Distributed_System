import java.io.IOException;
import java.net.InetAddress;
import java.util.*;


public class Customer extends stubUser
{
    Filter filter;
    Set<String> cart = new HashSet<>();
    String name;

    public Customer(String name)
    {
        super(name);
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

    public void search(Filter filter)
    {
        try
        {
            out.writeObject(filter);
            out.flush();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    public void buy()
    {
        try
        {
            out.writeObject(cart);
            out.flush();

            // Receive purchase confirmation
            Object response = in.readObject();
            System.out.println("Purchase response: " + response);

            cart.clear();
        }
        catch (IOException | ClassNotFoundException e)
        {
            System.err.println("Error during purchase: " + e.getMessage());
        }
    }

    public Object receiveMessageObject()
    {
        try
        {
            return in.readObject();
        }
        catch (IOException | ClassNotFoundException e)
        {
            System.err.println("Error receiving object: " + e.getMessage());
            return null;
        }
    }

    public void addToCart(String product)
    {
        cart.add(product);
    }
}
