package com.example.customerapp.view.shoppingCart;

import com.example.customerapp.view.base.BaseView;

import java.util.List;

import lib.shared.Product;

public interface ShoppingCartView extends BaseView
{
    void goToResultsActivity();
    public void populateProductsRecyclerView(List<Product> products);

    void updateCartUI(int idx);

    void showBuyMessageAsync(String title, String message);
    void showRatingMessageAsync(String title, String message);
}
