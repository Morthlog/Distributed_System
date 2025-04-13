
import org.json.simple.JSONObject;

import java.io.Serializable;

public class Product implements Serializable {
    private String productName;
    private String productType;
    private int availableAmount;
    private double price;
    private boolean hidden; // Hidden products when empty from customer but visible to manager

    public Product(String productName, String productType, int availableAmount, double price) {
        this(productName, productType, availableAmount, price, false);
    }

    public Product(String productName, String productType, int availableAmount, double price, boolean hidden) {
        this.productName = productName;
        this.productType = productType;
        this.availableAmount = availableAmount;
        this.price = price;
        this.hidden = hidden;
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

    public String getProductType() {
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

