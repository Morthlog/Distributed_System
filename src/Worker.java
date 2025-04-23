import java.io.*;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.*;
import static java.lang.Thread.sleep;

public class Worker extends Communication {

    // Hashmap<String, Store> for memory and backup
    private static final Map<String, ExtendedStore> memory = new HashMap<>(); // should be extendedStore
    private static final Map<String, ExtendedStore> backup = new HashMap<>(); // should be extendedStore
    /**
     * actionTable for every way the {@link Worker} should respond
     * current actions are not permanent
     */
    private static <T> T actionTable(BackendMessage<T> msg) {
        Client client = msg.getClient();
        RequestCode code = msg.getRequest();
        T val = msg.getValue();
        return switch (client) {
            case Customer -> switch (code) {
                case STUB_TEST_1-> sendString(val);
                case STUB_TEST_2 -> sendNum(val);
                case SEARCH -> (T) mapSearch((Filter) val);
                case BUY -> (T) buy((ShoppingCart) val);
                default -> {
                    System.err.println("Unknown customer code: " + code);
                    throw new RuntimeException();
                }
            };
            case Manager -> switch (code) {
                case 1 -> addStore(val);
                case 2 -> addProduct(val);
                case 3 -> removeProduct(val);
                case 4 -> manageStock(val);
                case 5 -> saveSale(val);
                case 6 -> displaySales(val);
                default -> {
                    System.err.println("Unknown manager code: " + code);
                    throw new RuntimeException();
                }
            };
            case MASTER -> switch (code){
                case TRANSFER_BACKUP -> transferToMemory(val);
                default -> {
                    System.err.println("Unknown MASTER code: " + code);
                    throw new RuntimeException();
                }
            };
        };

    }

    private static <T> T addStore(T val) {
        JSONObject storeJson = (JSONObject) val;
        ExtendedStore store = new ExtendedStore(storeJson);

        synchronized (memory) {
            if (memory.containsKey(store.getStoreName())) {
                return (T) "Store already exists";
            }

            memory.put(store.getStoreName(), store);
            backup.put(store.getStoreName(), store);
        }
        return (T) "Store added successfully";
    }

    private static <T> T addProduct(T val) {
        Map<String, Object> parameters = (Map<String, Object>) val;
        String storeName = (String) parameters.get("storeName");
        Product product = (Product) parameters.get("product");

        synchronized (memory) {
            ExtendedStore store = memory.get(storeName);
            if (store == null) {
                return (T) "Store not found";
            }

            store.addProduct(product);
            backup.put(storeName, store);
        }
        return (T) "Product added successfully";
    }

    private static <T> T removeProduct(T val) {
        Map<String, Object> parameters = (Map<String, Object>) val;
        String storeName = (String) parameters.get("storeName");
        String productName = (String) parameters.get("productName");

        synchronized (memory) {
            ExtendedStore store = memory.get(storeName);
            if (store == null) {
                return (T) "Store not found";
            }
            store.removeProduct(productName);
            backup.put(storeName, store);
        }
        return (T) "Product removed successfully";
    }


    private static <T> T manageStock(T val) {
        Map<String, Object> parameters = (Map<String, Object>) val;
        String storeName = (String) parameters.get("storeName");
        String productName = (String) parameters.get("productName");
        Integer newAmount = (Integer) parameters.get("newAmount");

        synchronized (memory) {
            ExtendedStore store = memory.get(storeName);
            if (store == null) {
                return (T) "Store not found";
            }

            boolean updated = store.manageStock(productName, newAmount);
            if (!updated) {
                return (T) "Product not found";
            }

            backup.put(storeName, store);
        }
        return (T) "Stock updated successfully";
    }

    private static <T> T saveSale(T val) {
        Map<String, Object> parameters = (Map<String, Object>) val;
        String storeName = (String) parameters.get("storeName");
        String productName = (String) parameters.get("productName");
        Integer quantity = (Integer) parameters.get("quantity");

        synchronized (memory) {
            ExtendedStore store = memory.get(storeName);
            if (store == null) {
                return (T) "Store not found";
            }
            Product product = store.getProducts().get(productName);
            if (product == null) {
                return (T) "Product not found";
            }
            if (product.getAvailableAmount() < quantity) {
                return (T) ("Insufficient stock. Available: " + product.getAvailableAmount());
            }
            store.recordSale(productName, quantity);
            backup.put(storeName, store);
        }
        return (T) "Sale recorded successfully";
    }

    private static <T> T  displaySales(T val) {
        String category = (String) val;
        Map<String, Integer> salesByStore = new HashMap<>();
        int total = 0;
        synchronized (memory) {
            for (ExtendedStore store : memory.values()) {
                int sales = store.getSalesByProductType(category);
                if (sales > 0) {
                    salesByStore.put(store.getStoreName(), sales);
                    total += sales;
                }
            }
        }
        salesByStore.put("total", total);
        return (T) salesByStore;
    }

    private static <T> T transferToMemory(T val){
        String storeName = (String)val;
        memory.put(storeName, backup.get(storeName));
        backup.remove(storeName);
        return (T)"OK";    }

    private static String buy(ShoppingCart shoppingCart)
    {
        try
        {
            // Simulate a delay so that the animation in DummyApp has time to play
            sleep(2000);
        }
        catch (InterruptedException e)
        {
            throw new RuntimeException(e);
        }
        Store store = memory.get(shoppingCart.getStoreName());

        synchronized (store)
        {
//            double cartValue = 0;
//
//            for (Map.Entry<String, Integer> entry : shoppingCart.getProducts().entrySet())
//            {
//                String productName = entry.getKey();
//                int quantity = entry.getValue();
//
//                store.recordSale(productName, quantity);
//
//                double price = store.getProductPrice(productName);
//                cartValue += price * quantity;
//            }
//
//            store.addRevenue(cartValue);

            return "Purchase completed for store: "+ store.getStoreName()+ " and products "+ shoppingCart.getProducts();
        }
    }



    private static List<Store> mapSearch(Filter filter)
    {
        try
        {
            // Simulate a delay so that the animation in DummyApp has time to play
            sleep(2000);
        }
        catch (InterruptedException e)
        {
            throw new RuntimeException(e);
        }
        List<Store> result = new ArrayList<>();
        for (Store store : memory.values())
        {
            if (storeMatchesFilter(store, filter))
            {
                result.add(store);
            }
        }
        return result;
    }


    private static boolean storeMatchesFilter(Store store, Filter filter)
    {
        double distance = calculateDistance(
                filter.getLatitude(), filter.getLongitude(),
                store.getLatitude(), store.getLongitude()
        );
        if (distance > 5.0)
            return false;

        // check food categories
        if (!matchesCategory(store.getFoodCategory(), filter.getFoodCategories()))
        {
            return false;
        }
        // check price categories
        if (!matchesCategory(store.getPriceCategory(), filter.getPriceCategories()))
        {
            return false;
        }
        // check minimum stars
        if (store.getStars() < filter.getStars())
        {
            return false;
        }
        return true;
    }

    private static<T> boolean matchesCategory(String storeCategory, T[] filterCategories)
    {
        for (T category : filterCategories)
        {
            if (storeCategory.equalsIgnoreCase(category.toString()))
            {
                return true;
            }
        }
        return false;
    }

    // Equirectangular Distance Approximation. Good and fast for our case (short distance).
    static double calculateDistance(double lat1, double lon1, double lat2, double lon2)
    {
        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);
        double lon1Rad = Math.toRadians(lon1);
        double lon2Rad = Math.toRadians(lon2);
        double meanLat= (lat1Rad + lat2Rad) / 2;
        double deltaLon = (lon2Rad - lon1Rad) * Math.cos(meanLat);
        double deltaLat = (lat2Rad - lat1Rad);
        double distance = Math.sqrt(deltaLon * deltaLon + deltaLat * deltaLat) *6371; // (6371 = Earth's radius approximation)

        return distance;
    }

    private static <T> T sendString(T val){
        return (T) (val + " changed");
    }

    private static <T> T sendNum(T val){
        return (T) Integer.valueOf((Integer) val + 100);
    }


    /**
     * Take the appropriate action based on the msg's value's type
     * @param msg {@link Message} containing client's request
     */
    public static <T> void ManageRequest(BackendMessage<T> msg, Worker client)
    {
        try{
            T value = actionTable(msg);
            msg.setValue(value);

            client.sendMessage(msg);
            client.stopConnection();
        } catch (Exception e) {
            System.err.printf("Could not connect to server with ip: %s", e.getMessage());
        }
    }
    public static <T> void main(String[] args){

        System.out.printf("Worker %s has started\n", args[0]);
        String ip;
        try
        {
            ip = InetAddress.getLocalHost().getHostAddress();
        }
        catch (UnknownHostException e)
        {
            throw new RuntimeException(e);
        }
        Worker client = new Worker();

        client.init(ip, Integer.parseInt(args[0]));

        while (true)
        {
            client = new Worker();
            System.out.println("Waiting for request...");
//            String msg = "hello server from worker #" + args[0] + " round " + i;
            Message<T> request;
            client.startConnection(ip, TCPServer.basePort + 1 + Integer.parseInt(args[0]));
            try{
                request = client.receiveMessage();
            }
            catch (Exception e)
            {
//                continue;
                System.err.printf("Could not receive request from %s.\n", e.getMessage());
                throw new RuntimeException(e);
            }
            Worker finalClient = client;
            Thread t = new Thread(() -> ManageRequest((BackendMessage<T>) request, finalClient));
            t.start();
            try
            {
                sleep(0);
            }
            catch (InterruptedException e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    private void init(String ip, int id) {
        System.out.println("Starting memory/backup initialization");
        while(true){
            Message<Store> request; // should be extended store
            startConnection(ip, TCPServer.basePort + 1 + id);
            System.out.println("Connection established");
            try{
                request = receiveMessage();
            }
            catch (Exception e) {
                System.err.printf("Could not receive request from %s.\n", e.getMessage());
                e.printStackTrace();
                throw new RuntimeException(e);
            }
            if (request.getRequest() == RequestCode.INIT_MEMORY){
                memory.put(request.getValue().getStoreName(), request.getValue());
            } else if (request.getRequest() == RequestCode.INIT_BACKUP) {
                backup.put(request.getValue().getStoreName(), request.getValue());
            } else if (request.getRequest() == RequestCode.END_INIT_MEMORY) {
                stopConnection();
                break;
            }
            else{
                System.out.println("Unknown request: " + request.getRequest());
                throw new RuntimeException();
            }

            stopConnection();
        }
        System.out.println("Memory/Backup initialization complete");
    }
}
