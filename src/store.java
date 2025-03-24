import java.util.ArrayList;
import java.util.List;


class Store {
    private String storeName;
    private double latitude;
    private double longitude;
    private String foodCategory;
    private int stars;
    private int noOfVotes;
    private String storeLogo;
    private List<Product> products;
    private String priceCategory;

    public Store(String storeName, double latitude, double longitude, String foodCategory, int stars, int noOfVotes, String storeLogo, List<Product> products) {
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

    public String getStoreName() { return storeName; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public String getFoodCategory() { return foodCategory; }
    public int getStars() { return stars; }
    public int getNoOfVotes() { return noOfVotes; }
    public String getStoreLogo() { return storeLogo; }
    public List<Product> getProducts() {return products;}
    public String getPriceCategory() {return priceCategory;}

    public void calculatePriceCategory() {
        if (products.isEmpty()) {
            priceCategory = "N/A";
            return;
        }

        double sum = 0;
        for (Product product : products) {
            sum += product.getPrice();
        }
        double average = sum / products.size();

        if (average <= 5) {
            priceCategory = "$";
        } else if (average <= 15) {
            priceCategory = "$$";
        } else {
            priceCategory = "$$$";
        }
    }

    public void addProduct(Product product) {
        products.add(product);
        calculatePriceCategory(); // Επαναϋπολογισμός κατηγορίας τιμής
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
}