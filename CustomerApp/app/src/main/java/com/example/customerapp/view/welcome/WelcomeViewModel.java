package com.example.customerapp.view.welcome;

import com.example.customerapp.view.base.BaseViewModel;
import com.example.customerapp.view.dao.CustomerDAO;
import com.example.customerapp.view.memorydao.CustomerDAOMemory;

public class WelcomeViewModel extends BaseViewModel<WelcomePresenter>
{
    @Override
    protected WelcomePresenter createPresenter()
    {
        CustomerDAO userDAO = new CustomerDAOMemory();

        presenter = new WelcomePresenter();
        presenter.setCustomerDAO(userDAO);

        return presenter;
    }
}
