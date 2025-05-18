package com.example.customerapp.view.welcome;

import com.example.customerapp.domain.Customer;
import com.example.customerapp.domain.Filter;
import com.example.customerapp.view.base.BasePresenter;
import com.example.customerapp.view.dao.CustomerDAO;
import com.example.customerapp.view.memorydao.FilterDAOMemory;

public class WelcomePresenter extends BasePresenter<WelcomeView>
{
    private CustomerDAO customerDao;

    public void setCustomerDAO(CustomerDAO customerDAO)
    {
        this.customerDao = customerDAO;
    }

    public void initializeDefaultFilters()
    {
        Filter defaultFilter = new Filter();
        FilterDAOMemory.setFilter(defaultFilter);
    }

    public void createCustomer(String name)
    {
        Customer customer = new Customer(name);
        customerDao.save(customer);
        view.goToFiltersActivity();
    }
}
