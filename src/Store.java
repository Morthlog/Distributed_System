
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class Store implements Serializable {
    final String storeName;
    final double latitude;
    final double longitude;
    final String foodCategory;
    final int stars;
    final int noOfVotes;
    final String storeLogo;
    private String priceCategory;
    private List<Product> products;

    public Store(String storeName, double latitude, double longitude, String foodCategory,
                 int stars, int noOfVotes, String storeLogo, List<Product> products) {
        this.storeName = storeName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.foodCategory = foodCategory;
        this.stars = stars;
        this.noOfVotes = noOfVotes;
        this.storeLogo = storeLogo;
        this.products = products;
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

        this.products = new ArrayList<>();
        JSONArray productsArray = (JSONArray) jsonObject.get("Products");
        if (productsArray != null) {
            for (Object productObj : productsArray) {
                JSONObject productJson = (JSONObject) productObj;
                Product product = new Product(
                        (String) productJson.get("ProductName"),
                        (String) productJson.get("ProductType"),
                        ((Number) productJson.get("Available Amount")).intValue(),
                        ((Number) productJson.get("Price")).doubleValue()
                );
                this.products.add(product);
            }
        }
    }

    private void calculatePriceCategory() {
        double sum = 0;
        for (Product product : products) {
            sum += product.getPrice();
        }
        double avgPrice = sum / products.size();
        if (avgPrice <= 5) {
            this.priceCategory = "$";
        } else if (avgPrice <= 15) {
            this.priceCategory = "$$";
        } else {
            this.priceCategory = "$$$";
        }
    }

    public void addProduct(Product product) {
        if (products == null) {
            products = new ArrayList<>();
        }
        products.add(product);
        calculatePriceCategory();
    }

    public void removeProduct(String productName) {
        for (int i = 0; i < products.size(); i++) {
            if (products.get(i).getProductName().equals(productName)) {
                products.remove(i);
                break;
            }
        }
        calculatePriceCategory();
    }
    
    public boolean manageStock(String productName, int newAmount) {
        if (products == null) {
            return false;
        }
        for (Product product : products) {
            if (product.getProductName().equals(productName)) {
                product.setAvailableAmount(newAmount);
                return true;
            }
        }
        return false;
    }

//    public JSONObject toJSONObject() {
//        JSONObject json = new JSONObject();
//        json.put("StoreName", storeName);
//        json.put("Latitude", latitude);
//        json.put("Longitude", longitude);
//        json.put("FoodCategory", foodCategory);
//        json.put("Stars", stars);
//        json.put("NoOfVotes", noOfVotes);
//        json.put("StoreLogo", storeLogo);
//        json.put("PriceCategory", priceCategory);
//
//        JSONArray productsArray = new JSONArray();
//        for (Product product : products) {
//            productsArray.add(product.toJSONObject());
//        }
//        json.put("Products", productsArray);
//
//        return json;
//    }

    public String getStoreName() {
        return storeName;
    }

    public List<Product> getProducts() {
        return products;
    }
    @Override
    public String toString() {
        String result = "{"
                + "\"StoreName\": \"" + storeName + "\", "
                + "\"Latitude\": " + latitude + ", "
                + "\"Longitude\": " + longitude + ", "
                + "\"FoodCategory\": \"" + foodCategory + "\", "
                + "\"Stars\": " + stars + ", "
                + "\"NoOfVotes\": " + noOfVotes + ", "
                + "\"StoreLogo\": \"" + storeLogo + "\", "
                + "\"Products\": [";

        for (int i = 0; i < products.size(); i++) {
            Product p = products.get(i);
            result += "{"
                    + "\"ProductName\": \"" + p.getProductName() + "\", "
                    + "\"ProductType\": \"" + p.getProductType() + "\", "
                    + "\"Available Amount\": " + p.getAvailableAmount() + ", "
                    + "\"Price\": " + p.getPrice()
                    + "}";

            if (i < products.size() - 1) {
                result += ", ";
            }
        }
        result += "]}";
        return result;
    }

    public String getFoodCategory()
    {
        return foodCategory;
    }

    public int getStars()
    {
        return stars;
    }
}
