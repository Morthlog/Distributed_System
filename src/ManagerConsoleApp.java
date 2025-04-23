import java.io.*;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Map;
import org.json.simple.JSONObject;
import org.json.simple.parser.*;
import java.net.InetAddress;

public class ManagerConsoleApp extends Communication {
    private static final Scanner scanner = new Scanner(System.in);

    public ManagerConsoleApp() {
    }

    private <T> Message <String> sendRequest(Message<T> request) {
        try {
            String ip = InetAddress.getLocalHost().getHostAddress();
            startConnection(ip, TCPServer.basePort);
            sendMessage(request);
            Message <String> response = receiveMessage();
            stopConnection();
            return response;
        } catch (Exception e) {
            System.err.println("Failed send request: " + e.getMessage());
            throw new RuntimeException();
        }
    }

    private Map<String, ExtendedStore> getStores() {
        Map<String, ExtendedStore> stores = new HashMap<>();
        try {
            String ip = InetAddress.getLocalHost().getHostAddress();
            startConnection(ip, TCPServer.basePort);
            Message<String> request = new Message<>("getStores", Client.Manager, RequestCode.GET_STORES);
            sendMessage(request);
            Message<Map<String, ExtendedStore>> response = receiveMessage();
            stores = response.getValue();
            stopConnection();
            return stores;
        } catch (Exception e) {
            System.err.println("Error getting stores: " + e.getMessage());
            throw new RuntimeException();
        }
    }

    private void addStore() {
        System.out.print("Enter json path of the new store you want to add: ");
        String path = scanner.nextLine();

        try (FileReader reader = new FileReader(path)) {
            JSONObject storeJson = (JSONObject) new JSONParser().parse(reader);
            ExtendedStore newStore = new ExtendedStore(storeJson);
            Message<ExtendedStore> request = new Message<>(newStore, Client.Manager, RequestCode.ADD_STORE);
            Message<String> response = sendRequest(request);
            System.out.println(response.getValue());
        } catch (Exception e) {
            System.err.println("Error adding store: " + e.getMessage());
        }
    }

    private void addProduct() {
        try {
            System.out.print("Enter the name of the store you want to add a product: ");
            String storeName = scanner.nextLine();
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
            ProductAddition addData =new ProductAddition(storeName, newProduct);
            Message<ProductAddition> request = new Message<>(addData, Client.Manager, RequestCode.ADD_PRODUCT);
            Message<String> response = sendRequest(request);
            System.out.println(response.getValue());
        } catch (Exception e) {
            System.err.println("Error adding product: " + e.getMessage());
        }
    }

    private void removeProduct() {
        try {
            System.out.print("Enter the name of the store you want to remove a product from: ");
            String storeName = scanner.nextLine();
            System.out.print("Enter the name of the product you want to remove: ");
            String productName = scanner.nextLine();

            ProductRemoval removeData = new ProductRemoval(storeName, productName);
            Message<ProductRemoval> request = new Message<>(removeData, Client.Manager, RequestCode.REMOVE_PRODUCT);
            Message<String> response = sendRequest(request);
            System.out.println(response.getValue());
        } catch (Exception e) {
            System.err.println("Error removing product: " + e.getMessage());
        }
    }

    private void manageStock() {
        try {
            System.out.print("Enter the name of the store you want to manage stock: ");
            String storeName = scanner.nextLine();
            System.out.print("Enter the name of the product you want to manage stock: ");
            String productName = scanner.nextLine();
            System.out.print("Enter quantity you want to add or remove: ");
            int quantityChange = scanner.nextInt();
            scanner.nextLine();

            StockChange stockData = new StockChange(storeName, productName, quantityChange);
            Message<StockChange> request = new Message<>(stockData, Client.Manager, RequestCode.MANAGE_STOCK);
            Message<String> response = sendRequest(request);
            System.out.println(response.getValue());
        } catch (Exception e) {
            System.err.println("Error managing stock: " + e.getMessage());
        }
    }
    
    private void displaySalesStatistics() {
        System.out.println("\n Sales statistics option:");
        System.out.println("1. By store type");
        System.out.println("2. By product category");
        System.out.println("3. For a specific store");

        int option = scanner.nextInt();
        scanner.nextLine();

        switch (option) {
            case 1:
                displaySalesByStoreType();
                break;
            case 2:
                displaySalesByProductType();
                break;
            case 3:
                displaySalesByStore();
                break;
            default:
                System.out.println("Invalid option selected");
        }
    }

    private void displaySalesByStoreType() {
        System.out.print("Enter store type you want to display sales: ");
        String storeType = scanner.nextLine();
        Message<String> request = new Message<>(storeType, Client.Manager, RequestCode.GET_SALES_BY_STORE_TYPE);
        Message<String> response = sendRequest(request);
        System.out.println(response.getValue());
    }

    private void displaySalesByProductType() {
        System.out.print("Enter product category you are interested in displaying sales: ");
        String category = scanner.nextLine();
        Message<String> request = new Message<>(category, Client.Manager, RequestCode.GET_SALES_BY_PRODUCT_TYPE);
        Message<String> response = sendRequest(request);
        System.out.println(response.getValue());
    }

    private void displaySalesByStore() {
        System.out.print("Enter the name of the store you want to display sales: ");
        String storeName = scanner.nextLine();
        Message<String> request = new Message<>(storeName, Client.Manager, RequestCode.GET_SALES_BY_STORE);
        Message<String> response = sendRequest(request);
        System.out.println(response.getValue());
    }


        public static void main (String[]args){
            ManagerConsoleApp manager = new ManagerConsoleApp();
            boolean app_running = true;

            while (app_running) {
                System.out.println("\n Enter your choice between 1-7: ");
                System.out.println("1. Add new store");
                System.out.println("2. Add new product to store");
                System.out.println("3. Remove product from store");
                System.out.println("4. Manage stock");
                System.out.println("5. Display sales by product");
                System.out.println("6. Exit");

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
                        manager.displaySalesStatistics();
                        break;
                    case 6:
                        app_running = false;
                        System.out.println("Exiting...");
                        break;
                    default:
                        System.out.println("Invalid option, please choose a number from 1 to 7.");
                }
            }
            scanner.close();
        }
}
