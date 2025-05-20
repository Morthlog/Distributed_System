import lib.shared.StoreNameProvider;

import java.io.Serializable;

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
