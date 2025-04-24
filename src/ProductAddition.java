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
