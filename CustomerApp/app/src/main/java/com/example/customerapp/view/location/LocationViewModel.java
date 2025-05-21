package com.example.customerapp.view.location;

import com.example.customerapp.view.base.BaseViewModel;

public class LocationViewModel extends BaseViewModel<LocationPresenter>
{
    @Override
    protected LocationPresenter createPresenter()
    {
        presenter = new LocationPresenter();
        return presenter;
    }
}
