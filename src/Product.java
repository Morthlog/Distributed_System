import java.io.Serializable;

public class Product implements Serializable {
    private final String productName;
    private final ProductType productType;
    private int availableAmount;
    private final double price;
    private boolean hidden; // Hidden products when empty from customer but visible to manager
    private int sellAmount;

    public Product(String productName, ProductType productType, int availableAmount, double price) {
        this(productName, productType, availableAmount, price, false);
    }

    public Product(String productName, ProductType productType, int availableAmount, double price, boolean hidden) {
        this.productName = productName;
        this.productType = productType;
        this.availableAmount = availableAmount;
        this.price = price;
        this.hidden = hidden;
        this.sellAmount = 0;
    }

//    public JSONObject toJSONObject() {
//        JSONObject json = new JSONObject();
//        json.put("ProductName", productName);
//        json.put("ProductType", productType);
//        json.put("Available Amount", availableAmount);
//        json.put("Price", price);
//        json.put("Hidden", hidden);
//        return json;
//    }

    public String getProductName() {
        return productName;
    }

    public ProductType getProductType() {
        return productType;
    }
    public int getAvailableAmount() {
        return availableAmount;
    }

    public void setAvailableAmount(int availableAmount) {
        this.availableAmount = availableAmount;
    }

    public double getPrice() {
        return price;
    }

    public double getSales(){
        return sellAmount * price;
    }

    public void addPurchase(int amount){
        sellAmount += amount;
    }

    private int getSellAmount(){
        return sellAmount;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }
    @Override
    public String toString() {
        return "Products{" +
                "productName='" + productName + '\'' +
                ", productType='" + productType + '\'' +
                ", availableAmount=" + availableAmount +
                ", price=" + price +
                '}';
    }
}

