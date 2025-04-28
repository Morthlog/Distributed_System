import java.io.Serializable;

class StockChange implements StoreNameProvider, Serializable {
    private String storeName;
    private String productName;
    private int quantityChange;

    public StockChange(String storeName, String productName, int quantityChange) {
        this.storeName = storeName;
        this.productName = productName;
        this.quantityChange = quantityChange;
    }

    @Override
    public String getStoreName() {
        return storeName;
    }

    public String getProductName() {
        return productName;
    }

    public int getQuantityChange() {
        return quantityChange;
    }
}
