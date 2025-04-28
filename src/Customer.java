import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Customer extends stubUser
{
    private final ShoppingCart shoppingCart = new ShoppingCart();
    private String ip;
    private final Map<String, Integer> storeRatings = new HashMap<>();

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
    }

    public void addToCart(String productName, int count)
    {
        shoppingCart.addProduct(productName, count);
    }

    public void addStoreNameToCart(String storeName)
    {
        shoppingCart.setStoreName(storeName);
    }

    public void rateStore(Callback<String> callback, String storeName, int rating)
    {
        startConnection(ip, TCPServer.basePort);

        int oldRating = storeRatings.getOrDefault(storeName, 0);

        RatingChange ratingChange = new RatingChange(storeName, oldRating, rating);
        Message<RatingChange> msg = new Message<>(ratingChange, Client.Customer, RequestCode.RATE_STORE);
        sendMessage(msg);

        Message<String> responseMsg = receiveMessage();
        String verification = responseMsg.getValue();
        callback.onComplete(verification);
        stopConnection();

        storeRatings.put(storeName, rating);
    }

    public void clearShoppingCart(){
        shoppingCart.clear();
    }

}
