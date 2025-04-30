import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

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
                case RATE_STORE -> (T) addRatingToStore((RatingChange) val);
                default -> {
                    System.err.println("Unknown customer code: " + code);
                    throw new RuntimeException();
                }
            };
            case Manager -> switch (code) {

                case ADD_STORE -> addStore(val);
                case ADD_PRODUCT -> addProduct(val);
                case REMOVE_PRODUCT -> removeProduct(val);
                case MANAGE_STOCK -> manageStock(val);
                case GET_SALES_BY_STORE_TYPE -> getSalesByStoreType(val);
                case GET_SALES_BY_PRODUCT_TYPE -> getSalesByProductType(val);
                case GET_SALES_BY_STORE -> getSalesByStore(val);
                case GET_STORES -> (T) getAllStores();
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
        ExtendedStore store = (ExtendedStore) val;
        String storeName = store.getStoreName();
        synchronized (memory) {
            if (memory.containsKey(storeName)) {
                return (T) "Store already exists";
            }
            memory.put(storeName, store);
        }
        return (T) "Store added successfully";
    }

    private static <T> T addProduct(T val) {
        ProductAddition data= (ProductAddition) val;
        ExtendedStore store = memory.get(data.getStoreName());
        boolean result;
        synchronized (store) {
            result = store.addProduct(data.getProduct());
        }
        if (result)
            return (T) "Product added successfully";
        return (T) "Product already exists";
    }

    private static <T> T removeProduct(T val) {
        ProductRemoval data = (ProductRemoval) val;
        ExtendedStore store = memory.get(data.getStoreName());
        synchronized (store) {
            store.removeProduct(data.getProductName());
        }
        return (T) "Product removed successfully";
    }


    private static <T> T manageStock(T val) {
        StockChange data = (StockChange) val;
        ExtendedStore store = memory.get(data.getStoreName());
        boolean updated = store.manageStock(data.getProductName(), data.getQuantityChange());
        if (!updated)
            return (T) "Product not found";
        return (T) "Stock updated successfully";
    }

    private static <T> T getSalesByStoreType(T val) {
        String storeType = (String) val;
        Map<String, Double> salesByStoreType = new HashMap<>();
        double total = 0.0;

        for (ExtendedStore store : memory.values()) {
            if (store.getFoodCategory().equalsIgnoreCase(storeType)) {
                synchronized (store) {
                    double sales = 0.0;
                    for (Double salesValue : store.getProductSales().values()) {
                        sales += salesValue;
                    }
                    if (sales > 0) {
                        salesByStoreType.put(store.getStoreName(), sales);
                        total += sales;
                    }
                }
            }
        }
        salesByStoreType.put("total", total);
        return (T) salesByStoreType;
    }

    private static <T> T getSalesByProductType(T val) {
        String productType = (String) val;
        Map<String, Double> salesByProductType = new HashMap<>();
        double total = 0.0;

        for (ExtendedStore store : memory.values()) {
            synchronized (store) {
                double sales = store.getSales(productType);
                if (sales > 0) {
                    salesByProductType.put(store.getStoreName(), sales);
                    total += sales;
                }
            }
        }
        salesByProductType.put("total", total);
        return (T) salesByProductType;
    }

    private static <T> T getSalesByStore(T val) {
        String storeName = (String) val;
        ExtendedStore store = memory.get(storeName);
        Map<String, Double> salesByStore = new HashMap<>();
        double total = 0.0;

        synchronized (store) {
            for (Map.Entry<String, Double> entry : store.getProductSales().entrySet()) {
                double sales = entry.getValue();
                if (sales > 0) {
                    salesByStore.put(entry.getKey(), sales);
                    total += sales;
                }
            }
        }
        salesByStore.put("total", total);
        return (T) salesByStore;
    }

    private static <T> Map<String, ExtendedStore> getAllStores() {
        return new HashMap<>(memory);
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
        ExtendedStore store = memory.get(shoppingCart.getStoreName());

        boolean purchaseCompleted = store.tryPurchase(shoppingCart);
        String state="completed";
        if (!purchaseCompleted)
        {
            state="failed";
        }
        return String.format("Purchase %s for store: %s and products %s", state, store.getStoreName(), shoppingCart.getProducts());
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

    public static String addRatingToStore(RatingChange ratingChange)
    {
        Store store = memory.get(ratingChange.getStoreName());
        float newRating = store.addRating(ratingChange);

        return String.format("Rating completed. New store rating: %.1f", newRating);
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

        try{
            System.out.println("Waiting for ping");
            client.startConnection(ip, TCPServer.basePort + 1 + Integer.parseInt(args[0]));
            client.stopConnection(); // simple ping
            System.out.println("Ping successful");
        }catch (Exception e) {
        }

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
            Message<ExtendedStore> request; // should be extended store
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