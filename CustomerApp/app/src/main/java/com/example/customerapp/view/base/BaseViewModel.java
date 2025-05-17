package com.example.customerapp.view.base;

import androidx.lifecycle.ViewModel;

public abstract class BaseViewModel<P extends BasePresenter<? extends BaseView>> extends ViewModel
{
    protected P presenter;

    /**
     * Default constructor.
     */
    protected BaseViewModel()
    {
        presenter = createPresenter();
    }

    /**
     * Creates, initializes the presenter and any associated DAO.
     *
     * @return The initialized presenter.
     */
    protected abstract P createPresenter();

    /**
     * Gets the presenter associated with this view model.
     *
     * @return The presenter.
     */
    public P getPresenter()
    {
        return presenter;
    }

    /**
     * Clears references.
     */
    @Override
    protected void onCleared()
    {
        super.onCleared();
        presenter.clearView();
    }
}
