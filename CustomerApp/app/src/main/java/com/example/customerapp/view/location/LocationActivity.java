package com.example.customerapp.view.location;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.lifecycle.ViewModelProvider;

import com.example.customerapp.R;
import com.example.customerapp.view.base.BaseActivity;
import com.example.customerapp.view.results.ResultsActivity;

public class LocationActivity extends BaseActivity<LocationViewModel> implements LocationView
{
    private EditText longitudeInput;
    private EditText latitudeInput;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);
        viewModel.getPresenter().setView(this);


        longitudeInput = findViewById(R.id.longitude_input);
        latitudeInput = findViewById(R.id.latitude_input);

        Button confirmButton = findViewById(R.id.confirm_button);
        confirmButton.setOnClickListener(v -> confirmButtonClicked());
    }

    private void confirmButtonClicked()
    {
        viewModel.getPresenter().setLocation(getLongitude(), getLatitude());
    }

    private Double getLongitude()
    {
        String longitudeTxt = longitudeInput.getText().toString().trim();
        return longitudeTxt.isEmpty() ? 0 : Double.parseDouble(longitudeTxt);
    }

    private Double getLatitude()
    {
        String latitudeTxt = latitudeInput.getText().toString().trim();
        return latitudeTxt.isEmpty() ? 0 : Double.parseDouble(latitudeTxt);
    }

    @Override
    public void goToResultsActivity()
    {
        Intent intent = new Intent(this, ResultsActivity.class);

        startActivity(intent);
        finish();
    }

    @Override
    protected LocationViewModel createViewModel()
    {
        return new ViewModelProvider(this).get(LocationViewModel.class);
    }
}
