package com.example.customerapp.view.results;

import com.example.customerapp.network.TcpCustomerService;
import com.example.customerapp.view.base.BaseViewModel;
import com.example.customerapp.view.dao.CustomerDAO;
import com.example.customerapp.view.memorydao.CustomerDAOMemory;


public class ResultsViewModel extends BaseViewModel<ResultsPresenter>
{

    @Override
    protected ResultsPresenter createPresenter()
    {
        presenter = new ResultsPresenter(new TcpCustomerService());
        return presenter;
    }
}
