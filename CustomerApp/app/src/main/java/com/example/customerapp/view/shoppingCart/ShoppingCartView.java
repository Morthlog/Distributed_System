package com.example.customerapp.view.shoppingCart;

import com.example.customerapp.view.base.BaseView;

import java.util.List;
import java.util.Map;

import lib.shared.Product;

public interface ShoppingCartView extends BaseView
{
    void goToResultsActivity();
    public void populateProductsRecyclerView(List<Product> products);

    void updateCartUI(int idx);

    void showMessageAsync(String title, String message);
}
