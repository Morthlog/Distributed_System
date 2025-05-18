package com.example.customerapp.view.dao;

import com.example.customerapp.domain.Customer;
import java.util.Set;

public interface CustomerDAO
{

    /**
     * Find a user by username.
     *
     * @param username User's username.
     * @return User or {@code null} if user doesn't exist.
     */
    Customer findByUsername(String username);

    /**
     * Saves a User object to the external data source.
     * The object can either be a new entity that does not exist
     * in the data source or an existing entity whose state
     * is being updated.
     *
     * @param user The object whose state is being saved
     *             to the external data source.
     */
    void save(Customer user);

    /**
     * Deletes the User object from the external data source.
     *
     * @param entity The object to be deleted.
     */
    void delete(Customer entity);

    /**
     * Retrieves all objects from the external data source.
     *
     * @return The list of objects.
     */
    Set<Customer> findAll();
}
