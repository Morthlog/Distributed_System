import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.Map;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.*;

public class ManagerConsoleApp {
    private static final String DATA_PATH = "src/Data/Stores.json";
    private static final Scanner scanner = new Scanner(System.in);
    final List<Store> stores = new ArrayList<>();
    final Map<String, Map<String, Integer>> productSales = new HashMap<>();

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
                stores.add(store);
                productSales.put(store.getStoreName(), new HashMap<>());

                for (Product product : store.getProducts()) {
                    productSales.get(store.getStoreName()).put(product.getProductType(), 0);
                }
            }
        } catch (Exception exception) {
            System.out.println("Error reading store data: " + exception.getMessage());
        }
    }

    private void saveStore() {
        JSONArray allStores = new JSONArray();

        for (Store currentstore : stores) {
            allStores.add(currentstore.toJSONObject());
        }
        try (FileWriter file = new FileWriter(DATA_PATH)) {
            file.write(allStores.toJSONString());
        } catch (IOException exception) {
            System.err.println("Save failed: " + exception.getMessage());
        }
    }

    private void addStore() {
        System.out.print("Enter path to JSON file: ");
        String path = scanner.nextLine();

        try (FileReader reader = new FileReader(path)) {
            JSONObject storeJson = (JSONObject) new JSONParser().parse(reader);
            Store newStore = new Store(storeJson);

            for (Store existingStore : stores) {
                if (existingStore.getStoreName().equals(newStore.getStoreName())) {
                    System.out.println("Store already exists.");
                    return;
                }
            }
            stores.add(newStore);
            productSales.put(newStore.getStoreName(), new HashMap<>());
            for (Product product : newStore.getProducts()) {
                productSales.get(newStore.getStoreName()).put(product.getProductType(), 0);
            }
            saveStore();
            System.out.println("Store added");
        } catch (Exception exception) {
            System.out.println("Error adding store: " + exception.getMessage());
        }
    }

    private void addProduct() {
        if (stores.isEmpty()) {
            System.out.println("No stores have been loaded yet.");
            return;
        }
        System.out.print("Enter store name: ");
        String storeName = scanner.nextLine();
        System.out.print("Product name: ");
        String productName = scanner.nextLine();
        System.out.print("Product type: ");
        String productType = scanner.nextLine();
        System.out.print("Available amount: ");
        int availableAmount = scanner.nextInt();
        System.out.print("Price: ");
        double price = scanner.nextDouble();
        scanner.nextLine();

        for (Store store : stores) {
            if (store.getStoreName().equalsIgnoreCase(storeName)) {
                Product newProduct= new Product(productName, productType, availableAmount, price);
                store.addProduct(newProduct);
                saveStore();
                System.out.println("Product added");
                return;
            }
        }
        System.out.println("Store not found!");
    }

    private void removeProduct() {
        System.out.print("Enter store name: ");
        String storeName = scanner.nextLine();
        System.out.print("Enter product name to remove: ");
        String productName = scanner.nextLine();

        for (Store store : stores) {
            if (store.getStoreName().equalsIgnoreCase(storeName)) {
                List<Product> products = store.getProducts();
                Product productToRemove =null;
                for (Product product : products) {
                    if (product.getProductName().equalsIgnoreCase(productName)) {
                        productToRemove = product;
                        break;
                    }
                }
                if (productToRemove != null) {
                    productToRemove.setHidden(true);
                    saveStore();
                    System.out.println("Product removed successfully.");
                    return;
                }
            }
        }
        System.out.println("Store not found!");
    }

    private void manageStock() {
        System.out.print("Enter store name: ");
        String storeName = scanner.nextLine();
        System.out.print("Enter product name: ");
        String productName = scanner.nextLine();
        System.out.print("Enter new available amount: ");
        int newAmount = scanner.nextInt();
        scanner.nextLine();

       for (Store store : stores) {
            if (store.getStoreName().equalsIgnoreCase(storeName)) {
                store.manageStock(productName, newAmount);
                saveStore();
                System.out.println("Stock updated successfully!");
                return;
            }
        }
        System.out.println("Store not found.");
    }

    private void saveSales() {
        System.out.print("Enter store name: ");
        String storeName = scanner.nextLine();
        System.out.print("Enter product name: ");
        String productName = scanner.nextLine();
        Store store = null;
        for (Store s : stores) {
            if (s.getStoreName().equalsIgnoreCase(storeName)) {
                store = s;
                break;
            }
        }
        if (store == null) {
            System.out.println("Store not found.");
            return;
        }
        Product product = null;
        for (Product p : store.getProducts()) {
            if (p.getProductName().equalsIgnoreCase(productName) && !p.isHidden()) {
                product = p;
                break;
            }
        }
        if (product == null) {
            System.out.println("Product not found.");
            return;
        }
        System.out.print("Enter quantity that have been sold: ");
        int quantity = scanner.nextInt();
        scanner.nextLine();
        int newAmount = product.getAvailableAmount() - quantity;
        if (newAmount < 0) {
            System.out.println("Insufficient stock. Available: " + product.getAvailableAmount());
            return;
        }
        product.setAvailableAmount(newAmount);
        String productType = product.getProductType();
        Map<String, Integer> storeSales = productSales.get(storeName);
        if (storeSales == null) {
            storeSales = new HashMap<>();
            productSales.put(storeName, storeSales);
        }
        int currentSales = storeSales.getOrDefault(productType, 0);
        storeSales.put(productType, currentSales + quantity);
        saveStore();
    }

    private void displaySalesByProduct() {
        System.out.print("Enter product category: ");
        String category = scanner.nextLine();

        System.out.println("\nSales by Product: " + category );
        Map<String, Integer> salesByStore = new HashMap<>();
        int total = 0;
        boolean found = false;
        for (Store store : stores) {
            String storeName = store.getStoreName();
            Map<String, Integer> storeSales = productSales.get(storeName);

            if (storeSales != null && storeSales.containsKey(category)) {
                found = true;
                int sales = storeSales.get(category);
                salesByStore.put(storeName, sales);
                total += sales;
            }
        }
        if (found) {
            for (Map.Entry<String, Integer> entry : salesByStore.entrySet()) {
                System.out.println("\"" + entry.getKey() + "\": " + entry.getValue());
            }
            System.out.println("\"total\": " + total);
        } else {
            System.out.println("No products found in " + category + " category.");
        }
    }
    public static void main(String[] args) {
        ManagerConsoleApp manager = new ManagerConsoleApp();
        boolean app_running = true;
        while (app_running) {
            System.out.print("Enter your choice: ");
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
