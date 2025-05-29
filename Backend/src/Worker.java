import lib.shared.*;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

public class Worker extends Communication {

    // Hashmap<String, lib.shared.Store> for memory and backup
    private static final Map<String, ExtendedStore> memory = new HashMap<>(); // should be extendedStore
    private static final Map<String, ExtendedStore> backup = new HashMap<>(); // should be extendedStore
    /**
     * actionTable for every way the {@link Worker} should respond
     */
    @SuppressWarnings("unchecked")
    private static <T> BackendMessage<T> actionTable(BackendMessage<T> msg) {
        Client client = msg.getClient();
        RequestCode code = msg.getRequest();
        SaveState saveState = msg.getSaveState();
        T val = msg.getValue();
        System.out.printf("Request: %s SaveState: %s\n", code, saveState);
        BackendMessage<T> response = switch (client) {
            case Customer -> switch (code) {
                case STUB_TEST_1-> (BackendMessage<T>) sendString((String) val);
                case STUB_TEST_2 -> (BackendMessage<T>) sendNum((Integer) val);
                case SEARCH -> (BackendMessage<T>) mapSearch((Filter) val);
                case BUY -> (BackendMessage<T>) buy((ShoppingCart) val, saveState);
                case RATE_STORE -> (BackendMessage<T>) addRatingToStore((RatingChange) val, saveState);
                default -> {
                    System.err.println("Unknown customer code: " + code);
                    throw new RuntimeException();
                }
            };
            case Manager -> switch (code) {
                case ADD_STORE -> (BackendMessage<T>) addStore((ExtendedStore) val, saveState);
                case ADD_PRODUCT -> (BackendMessage<T>) addProduct((ProductAddition) val, saveState);
                case REMOVE_PRODUCT -> (BackendMessage<T>) removeProduct((ProductRemoval) val, saveState);
                case MANAGE_STOCK -> (BackendMessage<T>) manageStock((StockChange) val, saveState);
                case GET_SALES_BY_STORE_TYPE -> (BackendMessage<T>) getSalesByStoreType((FoodCategory[]) val);
                case GET_SALES_BY_PRODUCT_TYPE -> (BackendMessage<T>) getSalesByProductType((ProductType[]) val);
                case GET_SALES_BY_STORE -> (BackendMessage<T>) getSalesByStore((ExtendedStore) val);
                case GET_STORES -> (BackendMessage<T>) getAllStores();
                default -> {
                    System.err.println("Unknown manager code: " + code);
                    throw new RuntimeException();
                }
            };
            case MASTER -> switch (code){
                case TRANSFER_TO_MEMORY -> (BackendMessage<T>) transferToMemory((Vector<String>) val);
                case TRANSFER_TO_BACKUP -> (BackendMessage<T>) transferToBackup((Vector<String>) val);
                case GET_STORES -> (BackendMessage<T>) getStores((Vector<String>) val);
                default -> {
                    System.err.println("Unknown MASTER code: " + code);
                    throw new RuntimeException();
                }
            };
        };
        response.setClient(client);
        response.setRequest(code);
        response.setId(msg.getId());
        response.setCallReducer(msg.isCallReducer());
        return response;
    }



    private static BackendMessage<String> addStore(ExtendedStore store, SaveState saveState) {
        String storeName = store.getStoreName();
        BackendMessage<String> msg = new BackendMessage<>();
        Map<String, ExtendedStore> database = getDatabaseFor(saveState);
        synchronized (database) {
            if (database.containsKey(storeName)) {
                msg.setValue("Store already exists");
            }
            else
            {
                msg.setValue("Store added successfully");
                database.put(storeName, store);
            }
        }
        setupForBackup(msg, saveState);
        return msg;
    }

    private static BackendMessage<String> addProduct(ProductAddition data, SaveState saveState) {

        Map<String, ExtendedStore> database = getDatabaseFor(saveState);
        ExtendedStore store;
        synchronized (database) {
            store = database.get(data.getStoreName());
        }
        boolean result;
        BackendMessage<String> msg = new BackendMessage<>();
        result = store.addProduct(data.getProduct(), saveState == SaveState.BACKUP);
        if (result)
        {
            msg.setValue("Product added successfully");
        }
        else
        {
            msg.setValue("Product already exists");
        }

        setupForBackup(msg, saveState);
        return msg;
    }

    private static BackendMessage<String> removeProduct(ProductRemoval data, SaveState saveState) {
        Map<String, ExtendedStore> database = getDatabaseFor(saveState);
        ExtendedStore store;
        synchronized (database) {
            store = database.get(data.getStoreName());
        }
        BackendMessage<String> msg = new BackendMessage<>();
        boolean result = store.removeProduct(data.getProductName());
        if (result)
        {
            msg.setValue("Product removed successfully");
        }
        else
        {
            msg.setValue("Product does not exist");
        }

        setupForBackup(msg, saveState);
        return msg;
    }


    private static BackendMessage<String> manageStock(StockChange data, SaveState saveState) {
        Map<String, ExtendedStore> database = getDatabaseFor(saveState);
        ExtendedStore store;
        synchronized (database) {
            store = database.get(data.getStoreName());
        }
        BackendMessage<String> msg = new BackendMessage<>();
        boolean updated = store.manageStock(data.getProductName(), data.getQuantityChange(), saveState == SaveState.BACKUP);
        if (updated)
        {
            msg.setValue("Stock updated successfully");
        }
        else
        {
            msg.setValue("Stock does not exist");
        }
        setupForBackup(msg, saveState);
        return msg;
    }

    private static BackendMessage<Map<String, Map<String, Double>>> getSalesByStoreType(FoodCategory[] categories)
    {
        Map<String, Map<String, Double>> result = new HashMap<>();

        Map<String, ExtendedStore> database = getDatabaseFor(SaveState.MEMORY);
        Collection<ExtendedStore> stores;
        synchronized (database)
        {
            stores = database.values();
        }

        for (ExtendedStore store : stores)
        {
            String storeName = store.getStoreName();
            FoodCategory storeCategory = FoodCategory.valueOf(store.getFoodCategory().toUpperCase());

            for (FoodCategory category : categories)
            {
                if (storeCategory == category)
                {
                    Map<String, Product> products = store.getProducts();

                    double sales = 0.0;
                    for (Product product : products.values())
                    {
                        synchronized (product){
                            sales += product.getSales();
                        }
                    }

                    if (sales > 0)
                    {
                        String categoryName = category.name();
                        if (!result.containsKey(categoryName))
                        {
                            result.put(categoryName, new HashMap<>());
                        }
                        result.get(categoryName).put(storeName, sales);
                    }
                    break;
                }
            }
        }

        BackendMessage<Map<String, Map<String, Double>>> msg = new BackendMessage<>();
        msg.setValue(result);
        return msg;
    }


    private static BackendMessage<Map<String, Map<String, Double>>> getSalesByProductType(ProductType[] types)
    {
        Map<String, Map<String, Double>> result = new HashMap<>();

        Map<String, ExtendedStore> database = getDatabaseFor(SaveState.MEMORY);
        Collection<ExtendedStore> stores;
        synchronized (database)
        {
            stores = database.values();
        }

        for (ExtendedStore store : stores)
        {
            String storeName = store.getStoreName();
            for (ProductType productType : types)
            {
                double sales = store.getSalesByProductType(productType);
                if (sales > 0)
                {
                    String typeName = productType.name();
                    if (!result.containsKey(typeName))
                    {
                        result.put(typeName, new HashMap<>());
                    }
                    result.get(typeName).put(storeName, sales);
                }
            }
        }

        BackendMessage<Map<String, Map<String, Double>>> msg = new BackendMessage<>();
        msg.setValue(result);
        return msg;
    }


    private static BackendMessage<Map<String, Double>> getSalesByStore(ExtendedStore storeProvided) {
        Map<String, Double> salesByStore = new HashMap<>();
        double total = 0.0;

        Map<String, ExtendedStore> database = getDatabaseFor(SaveState.MEMORY);
        ExtendedStore store;
        synchronized (database) {
            store = database.get(storeProvided.getStoreName());
        }

        Map<String, Product> products = store.getProducts();
        for (Product product : products.values()) {
            double sales;
            synchronized (product) {
                sales = product.getSales();
            }
            if (sales > 0) {
                salesByStore.put(product.getProductName(), sales);
                total += sales;
            }
        }

        salesByStore.put("total", total);
        BackendMessage<Map<String, Double>> msg = new BackendMessage<>();
        msg.setValue(salesByStore);
        return msg;
    }

    private static BackendMessage<Map<String, ExtendedStore>> getAllStores() {
        synchronized (memory) {
            return new BackendMessage<>(new HashMap<>(memory));
        }

    }

    private static BackendMessage<String> transferToMemory(Vector<String> storeNames){
        synchronized (memory) {
            synchronized (backup){
                for (String storeName : storeNames)
                {
                    ExtendedStore store = backup.remove(storeName);
                    if (store != null) // Race condition, was added to Master mapping, but not to Workers
                        memory.put(storeName, store);
                }

            }
        }
        return new BackendMessage<>("OK");
    }

    private static BackendMessage<String> buy(ShoppingCart shoppingCart, SaveState saveState)
    {
        Map<String, ExtendedStore> database = getDatabaseFor(saveState);
        ExtendedStore store;
        synchronized (database) {
            store = database.get(shoppingCart.getStoreName());
        }

        boolean purchaseCompleted = store.tryPurchase(shoppingCart, saveState == SaveState.BACKUP);

        String state="completed";
        if (!purchaseCompleted)
        {
            state="failed";
        }
        String result = String.format("Purchase %s for store: %s and products %s", state, store.getStoreName(), shoppingCart.getProducts());
        BackendMessage<String> msg = new BackendMessage<>(result);
        setupForBackup(msg, saveState);
        return msg;
    }



    private static BackendMessage<List<Store>> mapSearch(Filter filter)
    {
        List<Store> result = new ArrayList<>();
        Map<String, ExtendedStore> database = getDatabaseFor(SaveState.MEMORY);
        Collection<ExtendedStore> stores;
        synchronized (database) {
            stores = database.values();
        }
        for (Store store : stores)
        {
            if (storeMatchesFilter(store, filter))
            {
                result.add(store);
            }
        }
        return new BackendMessage<>(result);
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

    private static<T> boolean matchesCategory(String storeCategory, List<T> filterCategories)
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

    public static BackendMessage<String> addRatingToStore(RatingChange ratingChange, SaveState saveState)
    {
        Map<String, ExtendedStore> database = getDatabaseFor(saveState);
        ExtendedStore store;
        synchronized (database) {
            store = database.get(ratingChange.getStoreName());
        }
        float newRating = store.addRating(ratingChange);

        String result = String.format("Rating completed. New store rating: %.1f", newRating);
        BackendMessage<String> msg = new BackendMessage<>(result);
        setupForBackup(msg, saveState);
        return msg;
    }

    private static BackendMessage<String> sendString(String val){
        try {
            sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return new BackendMessage<> ((val + " changed"));
    }

    private static BackendMessage<Integer> sendNum(Integer val){
        try {
            sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return new BackendMessage<>(val + 100);
    }

    private static Map<String, ExtendedStore> getDatabaseFor(SaveState saveState){
        if (saveState == SaveState.MEMORY)
            return memory;
        return backup;
    }

    private static void setupForBackup(BackendMessage<String> msg, SaveState saveState) {
        if (saveState == SaveState.MEMORY)
            msg.setSaveState(SaveState.BACKUP);
    }

    private static BackendMessage<Vector<Store>> transferToBackup(Vector<String> storeNames) {
        Vector<Store> result = new Vector<>();
        for (String storeName : storeNames) // no sync needed, activity paused from Master
        {
            ExtendedStore store = memory.remove(storeName);
            result.add(store);
            backup.put(storeName, store);
        }

        return new BackendMessage<>(result);
    }

    private static BackendMessage<Vector<Store>> getStores(Vector<String> storeNames){
        Vector<Store> result = new Vector<>();
        for (String storeName : storeNames) // no sync needed, activity paused from Master
            result.add(memory.get(storeName));
        return new BackendMessage<>(result);
    }

    /**
     * Take the appropriate action based on the msg's value's type
     * @param msg {@link Message} containing client's request
     */
    public static <T> void ManageRequest(BackendMessage<T> msg, Worker client)
    {
        try{
            msg = actionTable(msg);
            if (msg.isCallReducer()) 
            {
                client.stopConnection();
                String ip; // Reducer ip
                try {
                    ip = InetAddress.getLocalHost().getHostAddress();
                } catch (UnknownHostException e) {
                    throw new RuntimeException(e);
                }
                client.startConnection(ip, Reducer.serverPort);
            }
            client.sendMessage(msg);
            client.stopConnection();
        } catch (Exception e) {
            System.err.printf("Could not connect to server with ip: %s", e.getMessage());
        }
    }
    public static <T> void main(String[] args){

        System.out.printf("Worker %s has started\n", args[0]);
        String ip; // Master ip
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