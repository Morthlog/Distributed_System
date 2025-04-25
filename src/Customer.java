import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

public class Customer extends stubUser
{
    private final ShoppingCart shoppingCart = new ShoppingCart();
    String ip;

    public Customer(String name)
    {
        super(name);
        try
        {
            setIp();
        }
        catch (UnknownHostException e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setIp() throws UnknownHostException
    {
        ip = InetAddress.getLocalHost().getHostAddress();
    }

    public void search(Filter filter, Callback<List<Store>> callback)
    {
        startConnection(ip, TCPServer.basePort);
        Message<Filter> msg = new Message<>(filter, Client.Customer, RequestCode.SEARCH);
        sendMessage(msg);

        Message<List<Store>> responseMsg = receiveMessage();
        List<Store> stores = responseMsg.getValue();
        stopConnection();
        callback.onComplete(stores);
    }

    public void buy(Callback<String> callback)
    {
        startConnection(ip, TCPServer.basePort);
        Message<ShoppingCart> msg = new Message<>(shoppingCart, Client.Customer, RequestCode.BUY);
        sendMessage(msg);

        // Receive purchase confirmation
        Message<String> responseMsg = receiveMessage();
        String verification = responseMsg.getValue();
        stopConnection();

        callback.onComplete(verification);
        shoppingCart.clear();
    }

    public void addToCart(String productName, int count)
    {
        shoppingCart.addProduct(productName, count);
    }

    public void addStoreNameToCart(String storeName)
    {
        shoppingCart.setStoreName(storeName);
    }
}
