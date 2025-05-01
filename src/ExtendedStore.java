import java.util.*;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;


public class ExtendedStore extends Store {
    private final Map<String, Product> products;
    private final Map<String, Double> productSales;

    public ExtendedStore(String storeName, double latitude, double longitude, String foodCategory,
                         int stars, int noOfVotes, String storeLogo, Map<String, Product> products) {
        super(storeName, latitude, longitude, foodCategory, stars, noOfVotes, storeLogo);

        this.products = products;
        this.productSales = new HashMap<>();

        for (Product product : products.values()) {
            String productName = product.getProductName();
            productSales.put(productName, 0.0);

            if (!product.isHidden()) {
                visibleProducts.put(product.getProductName(), product);
            }
        }
        calculatePriceCategory();
    }

    public ExtendedStore(JSONObject jsonObject) {
        super(jsonObject);

        this.products = new HashMap<>();
        this.productSales = new HashMap<>();

        JSONArray productList = (JSONArray) jsonObject.get("Products");

        if (productList != null) {
            for (Object productObj : productList) {
                JSONObject productJson = (JSONObject) productObj;

                String productName = (String) productJson.get("ProductName");
                ProductType productType = ProductType.fromString((String) productJson.get("ProductType")) ;
                int availableAmount = ((Number) productJson.get("Available Amount")).intValue();
                double price = ((Number) productJson.get("Price")).doubleValue();
                boolean hidden = productJson.containsKey("Hidden") && (boolean) productJson.get("Hidden");

                Product product = new Product(productName, productType, availableAmount, price, hidden);
                products.put(productName, product);

                if (!product.isHidden()) {
                    visibleProducts.put(productName, product);
                }

                productSales.put(productName, 0.0);
            }
        }
        calculatePriceCategory();
    }

    public boolean addProduct(Product product, boolean bypassChecks) {
        if (!bypassChecks && products.containsKey(product.getProductName()))
            return false;
        products.put(product.getProductName(), product);

        if (!product.isHidden()) {
            visibleProducts.put(product.getProductName(), product);
        }

        productSales.put(product.getProductName(), 0.0);

        calculatePriceCategory();
        return true;
    }

    public boolean removeProduct(String productName) {
        if (!products.containsKey(productName))
            return false;
        Product product = products.get(productName);
        if (product != null) {
            product.setHidden(true);
            visibleProducts.remove(productName);
        }
        calculatePriceCategory();
        return true;
    }

    public boolean manageStock(String productName, int amountChange, boolean bypassChecks) {
        if (products == null) {
            return false;
        }
        Product product = products.get(productName);
        if (product == null) {
            return false;
        }

        synchronized (product) {
            int currentAmount = product.getAvailableAmount();
            int newAmount = currentAmount + amountChange;

            if (!bypassChecks && newAmount < 0) {
                return false;
            }

            product.setAvailableAmount(newAmount);
            return true;
        }
    }

    public boolean manageStock(String productName, int amountChange) {
        return manageStock(productName,amountChange,false);
    }

    public boolean saveSale(String productName, int quantity, boolean bypassChecks) {
        if (!products.containsKey(productName)) {
            return false;
        }

        Product product = products.get(productName);
        synchronized (product) {
            if (!bypassChecks && product.isHidden())
                return false;
            int currentAmount = product.getAvailableAmount();

            if (!bypassChecks && currentAmount < quantity) {
                return false;
            }
            product.setAvailableAmount(currentAmount - quantity);

            double salesIncome = product.getPrice() * quantity;
            double currentSales = productSales.getOrDefault(productName, 0.0);
            productSales.put(productName, currentSales + salesIncome);

            return true;
        }
    }

    public boolean saveSale(String productName, int quantity) {
        return saveSale(productName,quantity,false);
    }

    public double getSalesByProductType(ProductType requestedType) {
        double totalSales = 0;

        for (Product product : products.values())
        {
            if (product.getProductType().equals(requestedType))
            {
                String name = product.getProductName();
                synchronized (productSales)
                {
                    totalSales += productSales.getOrDefault(name, 0.0);
                }
            }
        }
        return totalSales;
    }


    @Override
    public Map<String, Product> getProducts() {
        return new HashMap<>(products);
    }

    public Map<String, Double> getProductSales() {
        return new HashMap<>(productSales);
    }

    public Store toCustomerStore() {
        Store customerStore = new Store(
                this.storeName,
                this.latitude,
                this.longitude,
                this.foodCategory,
                this.stars,
                this.noOfVotes,
                this.storeLogo
        );

        for (Product product : this.visibleProducts.values()) {
            customerStore.visibleProducts.put(product.getProductName(), product);
        }
        customerStore.calculatePriceCategory();
        return customerStore;
    }

    private record SaleRecord(String productName, int quantity) {}

    public boolean tryPurchase(ShoppingCart cart, boolean bypassChecks)
    {
        Deque<SaleRecord> localQueue = new ArrayDeque<>();

        for (Map.Entry<String,Integer> entry : cart.getProducts().entrySet())
        {
            String name = entry.getKey();
            int quantity = entry.getValue();

            boolean saveCompleted = saveSale(name, quantity, bypassChecks);
            if (!saveCompleted)
            {
                revertLocalSales(localQueue);
                return false;
            }

            localQueue.addLast(new SaleRecord(name, quantity));
        }
        return true;
    }

    private void revertLocalSales(Deque<SaleRecord> queue)
    {
        while (!queue.isEmpty())
        {
            SaleRecord record = queue.removeLast();
            Product product = products.get(record.productName);
            synchronized (product)
            {
                product.setAvailableAmount(product.getAvailableAmount() + record.quantity);
                double productTotalSales = productSales.get(record.productName);
                double currentProductRevenue=record.quantity*product.getPrice();
                productSales.put(record.productName, productTotalSales - currentProductRevenue);
            }
        }
    }
}
