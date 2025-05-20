package com.example.customerapp.view.memorydao;


import lib.shared.Filter;

public class FilterDAOMemory
{
    private static Filter currentFilter;

    public static Filter getFilter()
    {
        return currentFilter;
    }

    public static void setFilter(Filter filter)
    {
        currentFilter = filter;
    }
}
