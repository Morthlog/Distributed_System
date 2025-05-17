package com.example.customerapp.view.base;

public abstract class BasePresenter<V extends BaseView>
{
    protected V view;

    /**
     * Sets the view reference for this presenter.
     *
     * @param view The view.
     */
    public void setView(V view)
    {
        this.view = view;
    }

    /**
     * Clears the referenced view from this presenter.
     */
    public void clearView()
    {
        this.view = null;
    }
}
