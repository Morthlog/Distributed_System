package com.example.customerapp.view.memorydao;

import com.example.customerapp.domain.Customer;

import java.util.ArrayList;
import java.util.List;

import lib.shared.Filter;
import lib.shared.Store;

public class StoreDAOMemory
{
    private static List<Store> stores;

    public static List<Store> getStores()
    {
        return new ArrayList<>(stores) ;
    }

    public static void setStores(List<Store> stores)
    {
        StoreDAOMemory.stores = stores;
    }

    public Store findByName(String name)
    {
        for (Store store : stores)
        {
            if (store.getStoreName().equalsIgnoreCase(name))
            {
                return store;
            }
        }
        return null;
    }
}
