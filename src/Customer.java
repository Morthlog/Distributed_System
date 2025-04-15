public class Customer extends stubUser
{
    private final ShoppingCart shoppingCart = new ShoppingCart();

    public Customer(String name)
    {
        super(name);;
    }

    public void search(Filter filter)
    {
        Message<Filter> msg = new Message<>(filter,Client.Customer,RequestCode.SEARCH);
        sendMessage(msg);
    }

    public void buy()
    {
        Message<ShoppingCart> msg = new Message<>(shoppingCart,Client.Customer,RequestCode.BUY);
        sendMessage(msg);

        // Receive purchase confirmation
        Message<String> responseMsg = receiveMessage();
        String value  = responseMsg.getValue();
        System.out.println("Purchase response: " + value);

        shoppingCart.clear();
    }

    public void addToCart(String productName, int count)
    {
        shoppingCart.addProduct(productName, count);
    }

    public void  addStoreNameToCart(String storeName)
    {
        shoppingCart.setStoreName(storeName);
    }
}
