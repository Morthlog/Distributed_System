import java.util.HashMap;
import java.util.Map;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

class Store {
    private String storeName;
    private double latitude;
    private double longitude;
    private String foodCategory;
    private int stars;
    private int noOfVotes;
    private String storeLogo;
    private String priceCategory;

    private Map<String, Product> products;
    private Map<String, Integer> productSales;

    public Store(String storeName, double latitude, double longitude, String foodCategory,
                 int stars, int noOfVotes, String storeLogo, Map<String, Product> products) {
        this.storeName = storeName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.foodCategory = foodCategory;
        this.stars = stars;
        this.noOfVotes = noOfVotes;
        this.storeLogo = storeLogo;

        this.products = products;
        this.productSales = new HashMap<>();

        for (Product product : products.values()) {
            String type = product.getProductType();
            productSales.put(type, 0);
        }
        calculatePriceCategory();
    }

    public Store(JSONObject jsonObject) {
        this.storeName = (String) jsonObject.get("StoreName");
        this.latitude = ((Number) jsonObject.get("Latitude")).doubleValue();
        this.longitude = ((Number) jsonObject.get("Longitude")).doubleValue();
        this.foodCategory = (String) jsonObject.get("FoodCategory");
        this.stars = ((Number) jsonObject.get("Stars")).intValue();
        this.noOfVotes = ((Number) jsonObject.get("NoOfVotes")).intValue();
        this.storeLogo = (String) jsonObject.get("StoreLogo");

        this.productSales = new HashMap<>();
        this.products = new HashMap<>();

        JSONArray productList = (JSONArray) jsonObject.get("Products");

        if (productList != null) {
            for (Object productObj : productList) {
                JSONObject productJson = (JSONObject) productObj;

                String productName = (String) productJson.get("ProductName");
                String productType = (String) productJson.get("ProductType");
                int availableAmount = ((Number) productJson.get("Available Amount")).intValue();
                double price = ((Number) productJson.get("Price")).doubleValue();

                Product product = new Product(productName, productType, availableAmount, price);

                products.put(productName, product);
                String type = product.getProductType();
                productSales.put(type, 0);
            }
        }
        calculatePriceCategory();
    }

    private void calculatePriceCategory() {
        double sum = 0.0;
        for (Product p : products.values()) {
            sum += p.getPrice();
        }
        double averagePrice = sum / products.size();
        if (averagePrice <= 5) {
            this.priceCategory = "$";
        } else if (averagePrice <= 15) {
            this.priceCategory = "$$";
        } else {
            this.priceCategory = "$$$";
        }
    }

    public void addProduct(Product product) {
        if (products == null) {
            products = new HashMap<>();
        }
        products.put(product.getProductName(), product);


        String type = product.getProductType();
        if (!productSales.containsKey(type)) {
            productSales.put(type, 0);
        }

        calculatePriceCategory();
    }

    public void removeProduct(String productName) {
        Product product = products.get(productName);
        if (product != null) {
            product.setHidden(true);  // Mark as hidden instead of removing
        }
        calculatePriceCategory();
    }

    public boolean manageStock(String productName, int newAmount) {
        if (products == null) {
            return false;
        }
        Product product = products.get(productName);
        product.setAvailableAmount(newAmount);
        return true;
    }

    public boolean recordSale(String productName, int quantity) {
        if (!products.containsKey(productName)) {
            return false;
        }

        Product product = products.get(productName);
        int currentAmount = product.getAvailableAmount();

        if (currentAmount < quantity) {
            return false;
        }
        product.setAvailableAmount(currentAmount - quantity);

        String productType = product.getProductType();
        int currentSales = productSales.getOrDefault(productType, 0);
        productSales.put(productType, currentSales + quantity);

        return true;
    }

    public int getSalesByProductType(String productType) {
        return productSales.getOrDefault(productType, 0);
    }

    public JSONObject toJSONObject() {
        JSONObject json = new JSONObject();
        json.put("StoreName", storeName);
        json.put("Latitude", latitude);
        json.put("Longitude", longitude);
        json.put("FoodCategory", foodCategory);
        json.put("Stars", stars);
        json.put("NoOfVotes", noOfVotes);
        json.put("StoreLogo", storeLogo);
        json.put("PriceCategory", priceCategory);

        JSONArray productsArray = new JSONArray();
        for (Product product : products.values()) {
            productsArray.add(product.toJSONObject());
        }
        json.put("Products", productsArray);

        return json;
    }

    public String getStoreName() {
        return storeName;
    }

    public Map<String, Product> getProducts() {
        return products;
    }

    public Map<String, Integer> getProductSales() {
        return productSales;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("{")
                .append("\"StoreName\": \"").append(storeName).append("\", ")
                .append("\"Latitude\": ").append(latitude).append(", ")
                .append("\"Longitude\": ").append(longitude).append(", ")
                .append("\"FoodCategory\": \"").append(foodCategory).append("\", ")
                .append("\"Stars\": ").append(stars).append(", ")
                .append("\"NoOfVotes\": ").append(noOfVotes).append(", ")
                .append("\"StoreLogo\": \"").append(storeLogo).append("\", ")
                .append("\"Products\": [");
        int i = 0;
        for (Product p : products.values()) {
            result.append("{")
                    .append("\"ProductName\": \"").append(p.getProductName()).append("\", ")
                    .append("\"ProductType\": \"").append(p.getProductType()).append("\", ")
                    .append("\"Available Amount\": ").append(p.getAvailableAmount()).append(", ")
                    .append("\"Price\": ").append(p.getPrice())
                    .append("}");
            if (i < products.size() - 1) {
                result.append(", ");
            }
            i++;
        }
        result.append("]}");
        return result.toString();
    }
}
