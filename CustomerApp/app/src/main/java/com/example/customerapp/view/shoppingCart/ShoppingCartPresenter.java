package com.example.customerapp.view.shoppingCart;

import com.example.customerapp.domain.Customer;
import com.example.customerapp.network.CustomerServices;
import com.example.customerapp.view.base.BasePresenter;

import com.example.customerapp.view.dao.CustomerDAO;
import com.example.customerapp.view.memorydao.CustomerDAOMemory;
import com.example.customerapp.view.memorydao.StoreDAOMemory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lib.shared.Product;
import lib.shared.ShoppingCart;
import lib.shared.Store;

public class ShoppingCartPresenter extends BasePresenter<ShoppingCartView>
{
    private final CustomerServices customerServices;
    Customer customer;
    private StoreDAOMemory storeDAOMemory;
    private CustomerDAO customerDao;

    //cache productList at every session, because MAP does not guaranty order
    private List<Product> products;

    public ShoppingCartPresenter(CustomerServices customerServices)
    {
        this.customerServices = customerServices;
    }

    public int getQuantity(String productName)
    {
        Map<String, Integer> products = customer.getShoppingCart().getProducts();
        Integer quantity = products.get(productName);
        return (quantity != null) ? quantity : 0;
    }

    public void setCustomerDAO(CustomerDAO customerDAO)
    {
        this.customerDao = customerDAO;
        customer = customerDao.findByUsername(CustomerDAOMemory.currentUserName);
    }

    public void setStoreDAOMemory(StoreDAOMemory storeDAOMemory)
    {
        this.storeDAOMemory = storeDAOMemory;
    }

    public void getStoreItems(String storeName)
    {
        Store currentStore = storeDAOMemory.findByName(storeName);
        products = new ArrayList<>(currentStore.getProducts().values());
        view.populateProductsRecyclerView(products);
    }

    public void onBuyClicked()
    {
        ShoppingCart cart = customer.getShoppingCart();
        new Thread(() ->
        {
            try
            {
                view.showLoadingAsync();
                String response = customerServices.buy(cart);
                view.hideLoadingAsync();
                view.showBuyMessageAsync("Order state", response);
                customer.clearShoppingCart();

            }
            catch (Exception e)
            {
                view.hideLoadingAsync();
                view.showBuyMessageAsync("Error", "Purchase failed");
            }
        }).start();
    }


    public void onRateStore(String storeName, int rating)
    {
        int oldRating = customer.getOldRating(storeName);
        new Thread(() ->
        {
            try
            {
                view.showLoadingAsync();
                String result = customerServices.rateStore(storeName, oldRating, rating);
                customer.saveRating(storeName, rating);
                view.hideLoadingAsync();
                view.showRatingMessageAsync("Rate state",result);
            }
            catch (Exception e)
            {
                view.hideLoadingAsync();
                view.showRatingMessageAsync("Error", "Rating failed");
            }
        }).start();
    }

    public void onSetStoreForCart(String storeName)
    {
        customer.setStoreForCart(storeName);
    }

    public void onQuantityIncrease(Product product)
    {
        customer.addToCart(product.getProductName(), 1);
        view.updateCartUI(products.indexOf(product));
        updateTotal();
    }

    public void onQuantityDecrease(Product product)
    {
        customer.addToCart(product.getProductName(), -1);
        view.updateCartUI(products.indexOf(product));
        updateTotal();
    }

    private void updateTotal()
    {
        double total = 0;
        Map<String, Integer> cartItems = customer.getShoppingCart().getProducts();

        for (Map.Entry<String, Integer> entry : cartItems.entrySet())
        {
            String productName = entry.getKey();
            int quantity = entry.getValue();
            for (Product p : products)
            {
                if (p.getProductName().equals(productName))
                {
                    total += p.getPrice() * quantity;
                    break;
                }
            }
        }

        view.updateTotal(total);
    }

}
