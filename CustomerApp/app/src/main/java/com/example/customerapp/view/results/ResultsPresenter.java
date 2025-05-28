package com.example.customerapp.view.results;

import com.example.customerapp.network.CustomerServices;
import com.example.customerapp.view.base.BasePresenter;
import com.example.customerapp.view.memorydao.FilterDAOMemory;
import com.example.customerapp.view.memorydao.StoreDAOMemory;

import java.util.List;

import lib.shared.Store;

public class ResultsPresenter extends BasePresenter<ResultsView>
{
    private CustomerServices customerServices;

    public ResultsPresenter(CustomerServices customerServices)
    {
        this.customerServices = customerServices;
    }

    public void searchStores()
    {
        view.showLoading();
        Thread thread = new Thread(() ->
        {
            try
            {
                List<Store> results = customerServices.searchStores(FilterDAOMemory.getFilter());
                StoreDAOMemory.setStores(results);
                view.hideLoadingAsync();
                view.populateStoresRecyclerViewAsync(results);
            }
            catch (Exception e)
            {
                view.hideLoadingAsync();
                view.showMessageAsync("Error", "Search failed");
            }
        }, "SearchThread");
        thread.start();
    }
}
