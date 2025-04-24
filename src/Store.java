import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class Store implements Serializable, StoreNameProvider{
    protected String storeName;
    protected double latitude;
    protected double longitude;
    protected String foodCategory;
    protected int stars;
    protected int noOfVotes;
    protected String storeLogo;
    protected String priceCategory;

    protected Map<String, Product> visibleProducts;

    public Store(String storeName, double latitude, double longitude, String foodCategory,
                 int stars, int noOfVotes, String storeLogo) {
        this.storeName = storeName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.foodCategory = foodCategory;
        this.stars = stars;
        this.noOfVotes = noOfVotes;
        this.storeLogo = storeLogo;

        this.visibleProducts = new HashMap<>();
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

        this.visibleProducts = new HashMap<>();

        JSONArray productList = (JSONArray) jsonObject.get("Products");

        if (productList != null) {
            for (Object productObj : productList) {
                JSONObject productJson = (JSONObject) productObj;

                String productName = (String) productJson.get("ProductName");
                String productType = (String) productJson.get("ProductType");
                int availableAmount = ((Number) productJson.get("Available Amount")).intValue();
                double price = ((Number) productJson.get("Price")).doubleValue();

                Product product = new Product(productName, productType, availableAmount, price);

                if (!product.isHidden()) {
                    visibleProducts.put(productName, product);
                }
            }
        }
        calculatePriceCategory();
    }

    protected void calculatePriceCategory() {
        double sum = 0.0;
        for (Product p : visibleProducts.values()) {
            sum += p.getPrice();
        }
        double averagePrice = sum / visibleProducts.size();
        if (averagePrice <= 5) {
            this.priceCategory = "$";
        } else if (averagePrice <= 15) {
            this.priceCategory = "$$";
        } else {
            this.priceCategory = "$$$";
        }
    }

    public String getStoreName() {
        return storeName;
    }
    
    public Map<String, Product> getVisibleProducts() {
        return visibleProducts;
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
        for (Product p : visibleProducts.values()) {
            result.append("{")
                    .append("\"ProductName\": \"").append(p.getProductName()).append("\", ")
                    .append("\"ProductType\": \"").append(p.getProductType()).append("\", ")
                    .append("\"Available Amount\": ").append(p.getAvailableAmount()).append(", ")
                    .append("\"Price\": ").append(p.getPrice())
                    .append("}");
            if (i < visibleProducts.size() - 1) {
                result.append(", ");
            }
            i++;
        }
        result.append("]}");
        return result.toString();
    }

    public Map<String, Product> getProducts() {
        return new HashMap<>(visibleProducts);
    }
    public String getFoodCategory()
    {
        return foodCategory;
    }
    public  String getPriceCategory () {
        return priceCategory;
    }
    public int getStars()
    {
        return stars;
    }

    public double getLatitude()
    {
        return latitude;
    }

    public double getLongitude()
    {
        return longitude;
    }
}
