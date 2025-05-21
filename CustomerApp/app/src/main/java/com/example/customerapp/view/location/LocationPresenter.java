package com.example.customerapp.view.location;

import com.example.customerapp.view.base.BasePresenter;
import com.example.customerapp.view.memorydao.FilterDAOMemory;

import lib.shared.Filter;

public class LocationPresenter extends BasePresenter<LocationView>
{
    public void setLocation(Double longitude, Double latitude)
    {
        if (longitude!=0 && latitude!=0)
        {
            Filter filter = FilterDAOMemory.getFilter();
            filter.setLatitude(latitude);
            filter.setLongitude(longitude);
        }
        view.goToResultsActivity();
    }
}
