import java.util.*;

import lib.shared.Product;
import lib.shared.ProductType;
import lib.shared.ShoppingCart;
import lib.shared.Store;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;


public class ExtendedStore extends Store {
    private final Map<String, Product> products;

    public ExtendedStore(String storeName, double latitude, double longitude, String foodCategory,
                         int stars, int noOfVotes, byte[] storeLogo, Map<String, Product> products) {
        super(storeName, latitude, longitude, foodCategory, stars, noOfVotes, storeLogo);

        this.products = products;

        for (Product product : products.values()) {

            if (!product.isHidden()) {
                visibleProducts.put(product.getProductName(), product);
            }
        }
        calculatePriceCategory();
    }

    public ExtendedStore(JSONObject jsonObject) {
        super(jsonObject);

        this.products = new HashMap<>();

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

            }
        }
        calculatePriceCategory();
    }

    public boolean addProduct(Product product, boolean bypassChecks) {
        synchronized (products) {
            if (!bypassChecks && products.containsKey(product.getProductName()))
                return false;
            products.put(product.getProductName(), product);
        }

        if (!product.isHidden()) {
            synchronized (visibleProducts)
            {
                visibleProducts.put(product.getProductName(), product);
            }
        }

        calculatePriceCategory();
        return true;
    }

    public boolean removeProduct(String productName) {
        Product product;
        synchronized (products)
        {
            if (!products.containsKey(productName))
                return false;
            product = products.get(productName);
        }
        if (product != null)
        {
            synchronized (product)
            {
                product.setHidden(true);
            }
            synchronized (visibleProducts)
            {
                visibleProducts.remove(productName);
            }
        }
        calculatePriceCategory();
        return true;
    }

    public boolean manageStock(String productName, int amountChange, boolean bypassChecks) {
        if (products == null)
            return false;

        Product product;
        synchronized (products) {
            product = products.get(productName);
        }

        if (product == null) {
            return false;
        }

        synchronized (product) {
            int currentAmount = product.getAvailableAmount();
            int newAmount = currentAmount + amountChange;

            if (!bypassChecks && newAmount < 0)
                return false;


            product.setAvailableAmount(newAmount);
            return true;
        }
    }

    public boolean manageStock(String productName, int amountChange) {
        return manageStock(productName,amountChange,false);
    }

    public boolean saveSale(String productName, int quantity, boolean bypassChecks) {
        Product product;
        synchronized (products)
        {
            if (!products.containsKey(productName))
                return false;
            product = products.get(productName);
        }
        synchronized (product) {
            if (!bypassChecks && product.isHidden())
                return false;
            int currentAmount = product.getAvailableAmount();

            if (!bypassChecks && currentAmount < quantity) {
                return false;
            }
            product.setAvailableAmount(currentAmount - quantity);

            product.addPurchase(quantity);

            return true;
        }
    }

    public boolean saveSale(String productName, int quantity) {
        return saveSale(productName,quantity,false);
    }

    public double getSalesByProductType(ProductType requestedType) {
        double totalSales = 0;
        Map<String, Product> productsStorage;
        synchronized (products) {
            productsStorage = getProducts();
        }
        for (Product product : productsStorage.values())
        {
            if (product.getProductType().equals(requestedType)) // product type never changes
            {
                synchronized (product)
                {
                    totalSales += product.getSales();
                }
            }
        }
        return totalSales;
    }


    @Override
    public Map<String, Product> getProducts() {
        synchronized (products) {
            return new HashMap<>(products);
        }
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

        Map<String, Product> productsStorage;
        synchronized (visibleProducts) {
            productsStorage = super.getProducts();
        }
        for (Product product : productsStorage.values()) {
            synchronized (product) {
                customerStore.getVisibleProducts().put(product.getProductName(), product);
            }
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
        Product product;
        while (!queue.isEmpty())
        {
            SaleRecord record = queue.removeLast();
            synchronized (products)
            {
                product = products.get(record.productName);
            }
            synchronized (product)
            {
                product.setAvailableAmount(product.getAvailableAmount() + record.quantity);
                product.addPurchase(-record.quantity);
            }
        }
    }
}
