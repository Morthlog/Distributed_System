package com.example.customerapp.view.base;

public interface BaseView
{
    /**
     * Displays an error message to the user.
     *
     * @param title   The title of the error.
     * @param message The message of the error.
     */
    void showMessage(String title, String message);

    void showLoadingAsync();

    void showLoading();

    void hideLoadingAsync();
}
