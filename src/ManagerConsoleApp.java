import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Map;
import org.json.simple.JSONObject;
import org.json.simple.parser.*;
import java.net.InetAddress;


public class ManagerConsoleApp extends Communication {
    private static final Scanner scanner = new Scanner(System.in);

    public ManagerConsoleApp() {
    }
    private int getIntInput() {
        int num = scanner.nextInt();
        scanner.nextLine();
        return num;
    }

    private String[] displayStores(Map<String, ExtendedStore> stores) {
        if (stores == null || stores.isEmpty()) {
            System.out.println("No stores available.");
            return new String[0];
        }
        System.out.println("Available stores: ");
        int i = 0;
        String[] keyMapping = new String[stores.size()];
        for (ExtendedStore store : stores.values()) {
            System.out.printf("%d: %s. Food Category: %s. Stars: %.1f. Price Category: %s%n",
                    i,
                    store.getStoreName(),
                    store.getFoodCategory(),
                    store.getStars(),
                    store.getPriceCategory());
            keyMapping[i++] = store.getStoreName();
        }
        return keyMapping;
    }

    private String chooseStore(Map<String, ExtendedStore> stores) {
        String[] keyMapping =  displayStores(stores);
        System.out.println("Enter the number of the store you want to view products for: ");
        int storeIndex = getIntInput();
        if (storeIndex < 0 || storeIndex > keyMapping.length){
            System.out.println("Invalid input.");
            return chooseStore(stores);
        }

        return keyMapping[storeIndex];
    }

    private String[] displayStoreProducts(ExtendedStore store) {
        Map<String, Product> products = store.getProducts();
        System.out.println("Products in " + store.getStoreName() + ": ");
        int i = 0;
        String[] keyMapping = new String[products.size()];

        for (Product product : products.values()){
            System.out.printf("%d: %s. Category: %s. Price: %.2f. Available Amount: %d. Purchasable: %b%n",
                    i, product.getProductName(), product.getProductType(), product.getPrice(), product.getAvailableAmount(), !product.isHidden());
            keyMapping[i] = product.getProductName();
            i++;
        }
        return keyMapping;
    }

    private String chooseProduct(ExtendedStore store) {
        String[] productNames = displayStoreProducts(store);
        System.out.println("Enter the number of the product you want to select:");
        int productIndex = getIntInput();
        return productNames[productIndex];

    }


    private <T, R> R sendRequest(Message<T> request) {
        try {
            String ip = InetAddress.getLocalHost().getHostAddress();
            startConnection(ip, TCPServer.basePort);
            request.setClient(Client.Manager);
            sendMessage(request);
            Message<R> response = receiveMessage();
            stopConnection();
            return response.getValue();
        } catch (Exception e) {
            System.err.println("Failed send request: " + e.getMessage());
            throw new RuntimeException();
        }
    }

    private Map<String, ExtendedStore> getStores() {
        try {
            Message<String> request = new Message<>("");
            
            request.setRequest(RequestCode.GET_STORES);
            Map<String, ExtendedStore> stores = sendRequest(request);
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
            Message<ExtendedStore> request = new Message<>(newStore);
            
            request.setRequest(RequestCode.ADD_STORE);

            String response= sendRequest(request);
            System.out.println(response);
        } catch (Exception e) {
            System.err.println("Error adding store: " + e.getMessage());
        }
    }

    private void addProduct() {
            Map<String, ExtendedStore> stores = getStores();
            String storeName = chooseStore(stores);
            ExtendedStore selectedStore = stores.get(storeName);
            System.out.println("Selected store: " + storeName + ", Type: " + selectedStore.getFoodCategory());

            System.out.print("Enter the name of the product you want to add: ");
            String productName = scanner.nextLine();

            ProductType productType =chooseProductType();
            System.out.print("Enter available amount: ");
            int availableAmount = getIntInput();
            System.out.print("Enter price: ");
            double price = scanner.nextDouble();
            scanner.nextLine();

            Product newProduct = new Product(productName, productType, availableAmount, price);
            ProductAddition addData = new ProductAddition(storeName, newProduct);

            Message<ProductAddition> request = new Message<>(addData);
            
            request.setRequest(RequestCode.ADD_PRODUCT);
            String response = sendRequest(request);
            System.out.println(response);
    }

    private ProductType chooseProductType()
    {
        System.out.println("Choose the product type by typing its number:");
        ProductType[] productTypes = ProductType.values();
        for (int i = 0; i < productTypes.length; i++)
        {
            System.out.println(i + ": " + productTypes[i]);
        }

        int choice = Integer.parseInt(scanner.nextLine());
        return productTypes[choice];
    }

    private void removeProduct() {
            Map<String, ExtendedStore> stores = getStores();
            String storeName = chooseStore(stores);
            ExtendedStore selectedStore = stores.get(storeName);
            System.out.println("Selected store: " + storeName + ", Type: " + selectedStore.getFoodCategory());

            String productName = chooseProduct(selectedStore);

            ProductRemoval removeData = new ProductRemoval(storeName, productName);
            Message<ProductRemoval> request = new Message<>(removeData);
            
            request.setRequest(RequestCode.REMOVE_PRODUCT);
            String response = sendRequest(request);
            System.out.println(response);
    }

    private void manageStock() {
            Map<String, ExtendedStore> stores = getStores();

            String storeName = chooseStore(stores);
            ExtendedStore selectedStore = stores.get(storeName);
            System.out.println("Selected store: " + storeName + ", Type: " + selectedStore.getFoodCategory());

            String productName = chooseProduct(selectedStore);
            Product selectedProduct = selectedStore.getProducts().get(productName);
            System.out.println("Current stock for '" + productName + "': " + selectedProduct.getAvailableAmount());
            System.out.print("Enter quantity you want to add or remove (use negative values to remove): ");
            int quantityChange = getIntInput();

            StockChange stockData = new StockChange(storeName, productName, quantityChange);
            Message<StockChange> request = new Message<>(stockData);
            
            request.setRequest(RequestCode.MANAGE_STOCK);

            String response = sendRequest(request);
            System.out.println(response);

    }

    private void displaySalesStatistics() {
        System.out.println("Sales statistics option:");
        System.out.println("1. By store type");
        System.out.println("2. By product category");
        System.out.println("3. For a specific store");
        System.out.println("Enter your choice (1-3): ");

        int option = getIntInput();

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
                System.out.println("Invalid option,please try again.");
        }
    }

    private void displaySalesByStoreType() {
        System.out.print("Enter the type of the store you want to display sales for (e.g. 'burgers'): ");
        String storeType = scanner.nextLine();


        Message<String> request = new Message<>(storeType);
        
        request.setRequest(RequestCode.GET_SALES_BY_STORE_TYPE);

        Map<String, Double> salesData = sendRequest(request);
        System.out.println("Sales by Store Type: " + storeType);
        Double total = salesData.remove("total");
        for (Map.Entry<String, Double> entry : salesData.entrySet()) {
            System.out.printf("Store: %s - Sales: $%.2f%n", entry.getKey(), entry.getValue());
        }
        System.out.println("Total sales: " + total);
    }

    private void displaySalesByProductType()
    {
        ProductType[] types = ProductType.values();
        System.out.println("Choose from the available product types by typing the product type's number separated by space (e.g., 1 5 2):");
        for (int i = 0; i < types.length; i++)
        {
            System.out.println(i + ": " + types[i]);
        }

        String chosenTypes = scanner.nextLine();
        String[] typeIndexes = chosenTypes.split("\\s+");
        List<ProductType> selectedTypes = new ArrayList<>();
        for (String indexString : typeIndexes)
        {
            int index = Integer.parseInt(indexString);
            selectedTypes.add(types[index]);
        }

        Message<ProductType[]> request = new Message<>(selectedTypes.toArray(new ProductType[0]));
        request.setRequest(RequestCode.GET_SALES_BY_PRODUCT_TYPE);

        Map<String, Double> salesData = sendRequest(request);

        System.out.println("Total sales per selected product type:");
        for (ProductType type : selectedTypes)
        {
            Double typeTotal = salesData.get(type.name());
            if (typeTotal != null)
            {
                System.out.printf("Product Type: %s - Total Sales: $%.2f%n", type, typeTotal);
            }
        }
    }




    private void displaySalesByStore() {
        Map<String, ExtendedStore> stores = getStores();
        String storeName = chooseStore(stores);
        System.out.println("Sales for Store: " + storeName);

        Message<ExtendedStore> request = new Message<>(stores.get(storeName));
        
        request.setRequest(RequestCode.GET_SALES_BY_STORE);

        Map<String, Double> salesData = sendRequest(request);
        Double total = salesData.remove("total");
        for (Map.Entry<String, Double> entry : salesData.entrySet()) {
            System.out.printf("Store: %s - Sales: $%.2f%n", entry.getKey(), entry.getValue());
        }
        System.out.println("Total sales: " + total);
    }


    public static void main (String[]args){
        ManagerConsoleApp manager = new ManagerConsoleApp();
        boolean app_running = true;
        System.out.println("Manager Console Application");

        while (app_running) {
            System.out.println("Enter your choice between (1-6): ");
            System.out.println("1. Add new store");
            System.out.println("2. Add new product to store");
            System.out.println("3. Remove product from store");
            System.out.println("4. Manage stock");
            System.out.println("5. Display sales by product");
            System.out.println("6. Exit");

            int choice = manager.getIntInput();

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
                    System.out.println("Invalid option, try again.");
            }
        }
        scanner.close();
    }
}

