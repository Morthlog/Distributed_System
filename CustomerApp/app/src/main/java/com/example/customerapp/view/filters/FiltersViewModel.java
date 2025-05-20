package com.example.customerapp.view.filters;

import com.example.customerapp.view.base.BaseViewModel;

public class FiltersViewModel extends BaseViewModel<FiltersPresenter>
{
    @Override
    protected FiltersPresenter createPresenter()
    {
        presenter = new FiltersPresenter();
        return presenter;
    }
}
