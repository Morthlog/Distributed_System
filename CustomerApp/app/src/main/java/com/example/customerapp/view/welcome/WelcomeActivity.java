package com.example.customerapp.view.welcome;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.lifecycle.ViewModelProvider;

import com.example.customerapp.R;
import com.example.customerapp.view.base.BaseActivity;
import com.example.customerapp.view.filters.FiltersActivity;

public class WelcomeActivity extends BaseActivity<WelcomeViewModel> implements WelcomeView
{
    private EditText nameInput;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        viewModel.getPresenter().setView(this);
        viewModel.getPresenter().initializeDefaultFilters();

        nameInput = findViewById(R.id.name_input);
        Button continueButton = findViewById(R.id.continue_button);

        continueButton.setOnClickListener(v -> continueButtonClicked());
    }

    private void continueButtonClicked()
    {
        viewModel.getPresenter().createCustomer(getName());
    }

    private String getName()
    {
        return nameInput.getText().toString().trim();
    }

    @Override
    public void goToFiltersActivity()
    {
        Intent intent = new Intent(this, FiltersActivity.class);

        startActivity(intent);
        finish();
    }

    @Override
    protected WelcomeViewModel createViewModel()
    {
        return new ViewModelProvider(this).get(WelcomeViewModel.class);
    }
}
