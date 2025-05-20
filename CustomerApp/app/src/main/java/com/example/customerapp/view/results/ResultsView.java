package com.example.customerapp.view.results;


import com.example.customerapp.view.base.BaseView;

import java.util.List;

import lib.shared.Product;
import lib.shared.Store;

public interface ResultsView extends BaseView
{
    void goToFiltersActivity();

    void populateStoresRecyclerView(List<Store> results);

    void showMessageAsync(String title, String message);

    void populateStoresRecyclerViewAsync(List<Store> stores);
}
