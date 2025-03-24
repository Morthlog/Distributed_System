import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.Map;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ManagerConsoleApp {
    private static List<Store> stores = new ArrayList<>();
    private static final Scanner scanner = new Scanner(System.in);
    private static final String JSON_PATH = "src/example.json";

    private static Map<String, Integer> productSales = new HashMap<>();

    public static void main(String[] args) {
        loadStoreFromJson();

        boolean app_running = true;
        while (app_running) {
            System.out.println("\nPlease select an option:");
            System.out.println("1. Add new product to store");
            System.out.println("2. Remove product from store");
            System.out.println("3. Display sales by product");
            System.out.println("4. Display store");
            System.out.println("5. Exit");
            System.out.print("Enter your choice: ");

            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    addProduct();
                    break;
                case 2:
                    removeProduct();
                    break;
                case 3:
                    displaySalesByProduct();
                    break;
                case 4:
                    displayStore();
                    break;
                case 5:
                    app_running = false;
                    System.out.println("Exit");
                    break;
                default:
                    System.out.println("Invalid option.");
            }
        }
        scanner.close();
    }

    private static void loadStoreFromJson() {
        try {
            JsonObject storeJson = JsonParser.parseReader(new FileReader(JSON_PATH)).getAsJsonObject();

            String storeName = storeJson.get("StoreName").getAsString();
            double latitude = storeJson.get("Latitude").getAsDouble();
            double longitude = storeJson.get("Longitude").getAsDouble();
            String foodCategory = storeJson.get("FoodCategory").getAsString();
            int stars = storeJson.get("Stars").getAsInt();
            int noOfVotes = storeJson.get("NoOfVotes").getAsInt();
            String storeLogo = storeJson.get("StoreLogo").getAsString();

            List<Product> products = new ArrayList<>();
            JsonArray productsArray = storeJson.getAsJsonArray("Products");

            for (JsonElement element : productsArray) {
                JsonObject productJson = element.getAsJsonObject();

                String productName = productJson.get("ProductName").getAsString();
                String productType = productJson.get("ProductType").getAsString();
                int availableAmount = productJson.get("Available Amount").getAsInt();
                double price = productJson.get("Price").getAsDouble();

                Product product = new Product(productName, productType, availableAmount, price);
                products.add(product);
            }
            Store store = new Store(storeName, latitude, longitude, foodCategory, stars, noOfVotes, storeLogo, products);
            stores.add(store);
        } catch (IOException e) {
            System.out.println("Error reading JSON file: " + e.getMessage());
        }
    }

    private static void saveStoreToJson() {
        if (stores.isEmpty()) {
            System.out.println("No store to save.");
            return;
        }
        Store store = stores.get(0);
        try (FileWriter writer = new FileWriter(JSON_PATH)) {
            writer.write(store.toString());
        } catch (IOException e) {
            System.out.println("Error saving store to JSON: " + e.getMessage());
        }
    }

    private static void addProduct() {
        if (stores.isEmpty()) {
            System.out.println("No stores have been loaded yet.");
            return;
        }
        Store store = stores.get(0);

        System.out.print("Enter product name: ");
        String productName = scanner.nextLine();

        System.out.print("Enter product type: ");
        String productType = scanner.nextLine();

        System.out.print("Enter available amount: ");
        int availableAmount = scanner.nextInt();

        System.out.print("Enter price: ");
        double price = scanner.nextDouble();
        scanner.nextLine();

        Product newProduct = new Product(productName, productType, availableAmount, price);
        store.addProduct(newProduct);
        saveStoreToJson();
    }

    private static void removeProduct() {
        Store store = stores.get(0);
        List<Product> products = store.getProducts();

        if (products.isEmpty()) {
            System.out.println("This store has no products.");
            return;
        }
        for (int i = 0; i < products.size(); i++) {
            Product p = products.get(i);
            System.out.println((i+1) + ". " + p.getProductName() + " (" + p.getProductType() + ") - " + p.getPrice() + "€");
        }
        System.out.print("Enter product name to remove: ");
        String productToRemove = scanner.nextLine();

        store.removeProduct(productToRemove);
        saveStoreToJson();
    }

    private static void displayStore() {
        if (stores.isEmpty()) {
            System.out.println("No store has been loaded yet.");
            return;
        }
        Store store = stores.get(0);
        System.out.println("\n Store Information:");
        System.out.println("Name: " + store.getStoreName());
        System.out.println("Category: " + store.getFoodCategory());
        System.out.println("Stars: " + store.getStars());
        System.out.println("NoOfVotes: " + store.getNoOfVotes());
        System.out.println("Price category: " + store.getPriceCategory());
        System.out.println("Latitude: " + store.getLatitude());
        System.out.println("Longitude: " + store.getLongitude());
        System.out.println("Logo: " + store.getStoreLogo());

        List<Product> products = store.getProducts();
        System.out.println("\nProducts:");
        for (Product product : products) {
            System.out.println("- " + product.getProductName() +
                    " (" + product.getProductType() + ") - " +
                    product.getPrice() + "€ - Available: " +
                    product.getAvailableAmount());
        }
    }

    private static void displaySalesByProduct() {
        if (productSales.isEmpty()) {
            System.out.println("No sales available.");
            return;
        }
        System.out.println("\nSales by Product: ");
        int total = 0;
        for (Map.Entry<String, Integer> entry : productSales.entrySet()) {
            System.out.println("\"" + entry.getKey() + "\": " + entry.getValue());
            total += entry.getValue();
        }
        System.out.println("\"total\": " + total);
    }

}

