package com.example.customerapp.view.base;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModel;

import com.example.customerapp.R;


public abstract class BaseActivity<V extends ViewModel> extends AppCompatActivity implements BaseView
{
    protected V viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        viewModel =  createViewModel();
    }

    @Override
    public void showMessage(String title, String message)
    {
        new AlertDialog.Builder(this)
                .setCancelable(true)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(R.string.ok, null)
                .create()
                .show();
    }

    /**
     * Creates and returns the viewModel associated with this activity.
     *
     * @return The viewModel.
     */
    protected abstract V createViewModel();
}
