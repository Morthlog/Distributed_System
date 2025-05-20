package com.example.customerapp.view.shoppingCart;

import com.example.customerapp.network.TcpCustomerService;
import com.example.customerapp.view.base.BaseViewModel;
import com.example.customerapp.view.memorydao.CustomerDAOMemory;
import com.example.customerapp.view.memorydao.StoreDAOMemory;


public class ShoppingCartViewModel extends BaseViewModel<ShoppingCartPresenter>
{
    @Override
    protected ShoppingCartPresenter createPresenter()
    {
        presenter = new ShoppingCartPresenter(new TcpCustomerService());
        presenter.setCustomerDAO(new CustomerDAOMemory());
        presenter.setStoreDAOMemory(new StoreDAOMemory());
        return presenter;
    }
}
