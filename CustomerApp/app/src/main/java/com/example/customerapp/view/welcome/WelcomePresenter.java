package com.example.customerapp.view.welcome;

import com.example.customerapp.domain.Customer;
import com.example.customerapp.view.base.BasePresenter;
import com.example.customerapp.view.dao.CustomerDAO;
import com.example.customerapp.view.memorydao.CustomerDAOMemory;
import com.example.customerapp.view.memorydao.FilterDAOMemory;

import lib.shared.Filter;

public class WelcomePresenter extends BasePresenter<WelcomeView>
{
    private CustomerDAO customerDao;

    public WelcomePresenter()
    {
        setDefaultFilters();
    }
    public void setCustomerDAO(CustomerDAO customerDAO)
    {
        this.customerDao = customerDAO;
    }

    public void setDefaultFilters()
    {
        Filter defaultFilter = new Filter();

        defaultFilter.setFoodCategories(defaultFilter.getAvailableFoodCategories());
        defaultFilter.setStars(1);
        defaultFilter.setPriceCategories(defaultFilter.getAvailablePrices());
        defaultFilter.setLatitude(37.9932963);
        defaultFilter.setLongitude(23.733413);
        FilterDAOMemory.setFilter(defaultFilter);

    }

    public void createCustomer(String name)
    {
        Customer customer = new Customer(name);
        customerDao.save(customer);
        CustomerDAOMemory.currentUserName=name;
        view.goToResultsActivity();
    }
}
