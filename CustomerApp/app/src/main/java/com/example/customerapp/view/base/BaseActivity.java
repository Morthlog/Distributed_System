package com.example.customerapp.view.base;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModel;

import com.example.customerapp.R;


public abstract class BaseActivity<V extends ViewModel> extends AppCompatActivity implements BaseView
{
    protected V viewModel;
    private Dialog loadingDialog;
    private AlertDialog messageDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        viewModel = createViewModel();
    }

    @Override
    public void showMessage(String title, String message)
    {
        if(messageDialog==null)
        {
            new AlertDialog.Builder(this)
                    .setCancelable(true)
                    .setTitle(title)
                    .setMessage(message)
                    .setPositiveButton(R.string.ok, null)
                    .create()
                    .show();
        }

        if (!messageDialog.isShowing())
        {
            messageDialog.show();
        }
    }

    protected void showLoading()
    {
        if (loadingDialog == null)
        {
            loadingDialog = new Dialog(this);
            loadingDialog.setCancelable(false);
            loadingDialog.setContentView(new ProgressBar(this));
        }

        if (!loadingDialog.isShowing())
        {
            loadingDialog.show();
        }
    }

    protected void hideLoading()
    {
        if (loadingDialog != null && loadingDialog.isShowing())
        {
            loadingDialog.dismiss();
        }
    }

    @Override
    public void showLoadingAsync()
    {
        runOnUiThread(this::showLoading);
    }

    @Override
    public void hideLoadingAsync()
    {
        runOnUiThread(this::hideLoading);
    }

    /**
     * Creates and returns the viewModel associated with this activity.
     *
     * @return The viewModel.
     */
    protected abstract V createViewModel();
}
