import java.io.*;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import static java.lang.Thread.sleep;

public class Worker extends Communication {

    // Hashmap<String, Store> for memory and backup
    private static final Map<String, ExtendedStore> memory = new HashMap<>();
    private static final Map<String, ExtendedStore> backup = new HashMap<>();
    /**
     * actionTable for every way the {@link Worker} should respond
     * current actions are not permanent
     */
   
    
    private static <T> T actionTable(Message<T> msg) {
        Client client = msg.getClient();
        int code = msg.getRequest();
        T val = msg.getValue();
        return switch (client) {
            case Customer -> switch (code) {
                case 1 -> sendString(val);
                case 2 -> sendNum(val);
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
    public static <T> void ManageRequest(Message<T> msg, Worker client)
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
        Worker client;
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
            Thread t = new Thread(() -> ManageRequest(request, finalClient));
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
}
