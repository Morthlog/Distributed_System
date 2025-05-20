package com.example.customerapp.network;
import java.util.List;

import lib.shared.Filter;
import lib.shared.ShoppingCart;
import lib.shared.Store;

public interface CustomerServices
{
    List<Store>  searchStores(Filter filter) throws Exception;

    String placeOrder(ShoppingCart cart) throws Exception;

    String rateStore(String storeName, int oldRating, int newRating) throws Exception;
}
