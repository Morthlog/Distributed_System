import java.io.*;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Map;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.*;

public class ManagerConsoleApp {
    private static final String DATA_PATH = "src/Data/Stores.json";
    private static final Scanner scanner = new Scanner(System.in);

    private Map<String, Store> stores = new HashMap<>();

    public ManagerConsoleApp() {
        loadStoresFromJson();
    }

    private void loadStoresFromJson() {
        File file = new File(DATA_PATH);
        if (!file.exists()) {
            System.out.println("No store found");
            return;
        }
        JSONArray storesArray;
        try {
            Object temp = new JSONParser().parse(new FileReader(DATA_PATH));
            storesArray = (JSONArray) ((JSONObject) temp).get("Stores");
            for (Object obj : storesArray) {
                Store store = new Store((JSONObject) obj);
                stores.put(store.getStoreName(), store);
            }
        } catch (Exception e) {
            System.out.println("Error reading file: " + e.getMessage());
        }
    }

    private void saveStore() {
        JSONArray allStores = new JSONArray();
        JSONObject root = new JSONObject();

        for (Store s : stores.values()) {
            allStores.add(s.toJSONObject());
        }
        root.put("Stores", allStores);

        try (FileWriter file = new FileWriter(DATA_PATH)) {
            file.write(root.toJSONString());
        } catch (Exception e) {
            System.out.println("Error saving file: " + e.getMessage());
        }
    }

    private void addStore() {
        System.out.print("Enter json path of the new store: ");
        String path = scanner.nextLine();

        try (FileReader reader = new FileReader(path)) {
            JSONObject storeJson = (JSONObject) new JSONParser().parse(reader);
            Store newStore = new Store(storeJson);
            String storeName = newStore.getStoreName();

            if (stores.containsKey(storeName)) {
                System.out.println("Store already exists");
                return;
            }
            stores.put(storeName, newStore);
            saveStore();
        } catch (Exception e) {
            System.out.println("Error adding store: " + e.getMessage());
        }
    }

    private void addProduct() {
        System.out.print("Enter store name tou want to add a product: ");
        String storeName = scanner.nextLine();
        if (!stores.containsKey(storeName)) {
            System.out.println("Sorry,store doesn't exist");
            return;
        }

        System.out.print("Product name: ");
        String productName = scanner.nextLine();
        System.out.print("Product type: ");
        String productType = scanner.nextLine();
        System.out.print("Available amount: ");
        int availableAmount = scanner.nextInt();
        System.out.print("Price: ");
        double price = scanner.nextDouble();
        scanner.nextLine();

        Product newProduct = new Product(productName, productType, availableAmount, price);
        stores.get(storeName).addProduct(newProduct);
        saveStore();
        System.out.println("Product added successfully");
    }

    private void removeProduct() {
        System.out.print("Enter store name you want to remove a product: ");
        String storeName = scanner.nextLine();

        if (!stores.containsKey(storeName)) {
            System.out.print("Store not found!");
            return;
        }
        System.out.print("Name of the product to be removed: ");
        String productName = scanner.nextLine();

        Store store = stores.get(storeName);
        store.removeProduct(productName);
        saveStore();
        System.out.println("Product removed");
    }

    private void manageStock() {
        System.out.print("Enter store name you want to manage stock: ");
        String storeName = scanner.nextLine();

        if (!stores.containsKey(storeName)) {
            System.out.println("Store not found");
            return;
        }

        System.out.println("Enter product name: ");
        String productName = scanner.nextLine();
        System.out.println("Enter new available amount: ");
        int newAmount = scanner.nextInt();
        scanner.nextLine();

        Store store = stores.get(storeName);
        boolean updatedStock = store.manageStock(productName, newAmount);

        if (updatedStock) {
            saveStore();
            System.out.println("Stock updated");
        } else {
            System.out.println("Product not found");
        }
    }

    private void saveSales() {
        System.out.println("Enter store name you want to save a sale: ");
        String storeName = scanner.nextLine();

        if (!stores.containsKey(storeName)) {
            System.out.println("Store not found");
            return;
        }

        System.out.println("Enter product name: ");
        String productName = scanner.nextLine();

        Store store = stores.get(storeName);
        Map<String, Product> products = store.getProducts();

        if (!products.containsKey(productName)) {
            System.out.println("Product not found");
            return;
        }

        Product product = products.get(productName);
        if (product.isHidden()) {
            System.out.println("This product is currently not available");
            return;
        }

        System.out.println("Enter quantity that has been sold: ");
        int quantity = scanner.nextInt();
        scanner.nextLine();

        boolean successSaved = store.recordSale(productName, quantity);
        if (successSaved) {
            saveStore();
            System.out.println("Sale saved");
        } else {
            System.out.println("Insufficient stock. Available only: " + product.getAvailableAmount());
        }
    }

    private void displaySalesByProduct() {
        System.out.println("Enter product category you are interested in displaying: ");
        String category = scanner.nextLine();

        Map<String, Integer> salesByStore = new HashMap<>();
        int total = 0;
        boolean found = false;

        for (Store store :stores.values()) {
            int count = store.getSalesByProductType(category);
            if (count > 0) {
                salesByStore.put(store.getStoreName(), count);
                total += count;
            }
        }

        if (salesByStore.isEmpty()) {
            System.out.println("No sales found.");
        }else{
            salesByStore.forEach((name, count) -> System.out.println(name + ": " + count));
            System.out.println("Total sales in category: " + total);
        }
    }

    public static void main(String[] args) {
        ManagerConsoleApp manager = new ManagerConsoleApp();
        boolean app_running = true;

        while (app_running) {
            System.out.println("Enter your choice: ");
            System.out.println("1. Add new store");
            System.out.println("2. Add new product to store");
            System.out.println("3. Remove product from store");
            System.out.println("4. Manage stock");
            System.out.println("5. Save a sale");
            System.out.println("6. Display sales by product");
            System.out.println("7. Exit");

            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    manager.addStore();
                    break;
                case 2:
                    manager.addProduct();
                    break;
                case 3:
                    manager.removeProduct();
                    break;
                case 4:
                    manager.manageStock();
                    break;
                case 5:
                    manager.saveSales();
                    break;
                case 6:
                    manager.displaySalesByProduct();
                    break;
                case 7:
                    app_running = false;
                    break;
                default:
                    System.out.println("Invalid option, please try again.");
            }
        }
        scanner.close();
    }
}
