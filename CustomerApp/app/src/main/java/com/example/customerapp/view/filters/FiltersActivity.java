package com.example.customerapp.view.filters;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;

import com.example.customerapp.R;
import com.example.customerapp.view.base.BaseActivity;
import com.example.customerapp.view.results.ResultsActivity;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

import lib.shared.FoodCategory;


public class FiltersActivity extends BaseActivity<FiltersViewModel> implements FiltersView
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filters);
        viewModel.getPresenter().setView(this);

        setupButtons();
    }

    private void setupButtons()
    {
        MaterialCardView foodCard    = findViewById(R.id.card_food);
        MaterialCardView priceCard   = findViewById(R.id.card_price);
        MaterialCardView starsCard   = findViewById(R.id.card_stars);
        Button continueButton = findViewById(R.id.continue_button);

        foodCard.setOnClickListener(v -> viewModel.getPresenter().onShowFoodCategoryDialog());
        priceCard.setOnClickListener(v -> viewModel.getPresenter().onShowPriceDialog());
        starsCard.setOnClickListener(v -> viewModel.getPresenter().onShowStarsDialog());
        continueButton.setOnClickListener(v -> continueClicked());
    }

    @Override
    public void showPriceDialog(List<String> availablePrices, boolean[] checkedItems)
    {
        new AlertDialog.Builder(this)
                .setTitle("Select Price Categories")
                .setMultiChoiceItems(
                        availablePrices.toArray(new String[0]),
                        checkedItems,
                        (dialog, which, isChecked) -> viewModel.getPresenter().onPriceSelected(which, isChecked)
                )
                .setPositiveButton("OK",
                        (dialog, which) -> viewModel.getPresenter().onConfirmPriceSelection())
                .show();
    }

    @Override
    public void showStarsDialog(int[] availableStars, int defaultIndex)
    {
        String[] stars = new String[availableStars.length];
        for (int i = 0; i < availableStars.length; i++)
        {
            stars[i] = String.valueOf(availableStars[i]);
        }

        new AlertDialog.Builder(this)
                .setTitle("Select Stars")
                .setSingleChoiceItems(
                        stars,
                        defaultIndex,
                        (dialog, which) -> viewModel.getPresenter().onConfirmStarsSelection(availableStars[which]))
                .setPositiveButton("ok", null)
                .show();
    }

    @Override
    public void showFoodCategoryDialog(List<FoodCategory> availableCategories, boolean[] checkedItems)
    {
        String[] categoryNames = new String[availableCategories.size()];
        for (int i = 0; i < availableCategories.size(); i++)
        {
            categoryNames[i] = availableCategories.get(i).toString();
        }

        new AlertDialog.Builder(this)
                .setTitle("Select Categories")
                .setMultiChoiceItems(categoryNames, checkedItems,
                        (dialog, which, isChecked) ->  viewModel.getPresenter().onFoodCategorySelected(which, isChecked))
                .setPositiveButton("OK",
                        (dialog, which) -> viewModel.getPresenter().onConfirmFoodCategorySelection())
                .show();
    }

    @Override
    protected FiltersViewModel createViewModel()
    {
        return new ViewModelProvider(this).get(FiltersViewModel.class);
    }

    private void continueClicked()
    {
        Intent intent = new Intent(this, ResultsActivity.class);
        startActivity(intent);
        finish();
    }
}