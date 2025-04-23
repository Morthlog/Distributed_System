import java.io.Serializable;

class ProductAddition implements StoreNameProvider, Serializable {
    private String storeName;
    private Product product;

    public ProductAddition(String storeName, Product product) {
        this.storeName = storeName;
        this.product = product;
    }
    @Override
    public String getStoreName() {
        return storeName;
    }
    public Product getProduct() {
        return product;
    }
}

class ProductRemoval implements StoreNameProvider, Serializable {
    private String storeName;
    private String productName;

    public ProductRemoval(String storeName, String productName) {
        this.storeName = storeName;
        this.productName = productName;
    }
    @Override
    public String getStoreName() {
        return storeName;
    }
    public String getProductName() {
        return productName;
    }
}

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

class SaleRecord implements StoreNameProvider, Serializable {
    private String storeName;
    private String productName;
    private int quantity;

    public SaleRecord(String storeName, String productName, int quantity) {
        this.storeName = storeName;
        this.productName = productName;
        this.quantity = quantity;
    }

    @Override
    public String getStoreName() {
        return storeName;
    }
    public String getProductName() {
        return productName;
    }
    public int getQuantity() {
        return quantity;
    }
}