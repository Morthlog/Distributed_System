package com.example.customerapp.view.memorydao;

import com.example.customerapp.domain.Customer;
import com.example.customerapp.view.dao.CustomerDAO;

import java.util.HashSet;
import java.util.Set;

public class CustomerDAOMemory implements CustomerDAO
{
    public static String currentUserName;
    protected static Set<Customer> customers = new HashSet<>();

    @Override
    public void delete(Customer user)
    {
        customers.remove(user);
    }

    @Override
    public Set<Customer> findAll()
    {
        return new HashSet<>(customers);
    }

    @Override
    public void save(Customer user)
    {
        customers.add(user);
    }


    public Customer findByUsername(String username)
    {
        for (Customer customer : customers)
        {
            if (customer.getName().equalsIgnoreCase(username))
            {
                return customer;
            }
        }
        return null;
    }
}
