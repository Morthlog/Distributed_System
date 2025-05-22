package com.example.customerapp.view.filters;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;

import com.example.customerapp.R;
import com.example.customerapp.view.base.BaseActivity;
import com.example.customerapp.view.results.ResultsActivity;

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
        Button foodCategoryButton = findViewById(R.id.food_category_button);
        Button priceCategoryButton = findViewById(R.id.price_category_button);
        Button starsButton = findViewById(R.id.stars_button);
        Button continueButton = findViewById(R.id.continue_button);

        foodCategoryButton.setOnClickListener(v -> viewModel.getPresenter().onShowFoodCategoryDialog());
        priceCategoryButton.setOnClickListener(v -> viewModel.getPresenter().onShowPriceDialog());
        starsButton.setOnClickListener(v -> viewModel.getPresenter().onShowStarsDialog());
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
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_checkboxes, null);
        LinearLayout checkboxContainer = dialogView.findViewById(R.id.checkboxContainer);

        for (int i = 0; i < availableCategories.size(); i++)
        {
            FoodCategory category = availableCategories.get(i);
            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(category.name());
            checkBox.setChecked(checkedItems[i]);
            int index = i;
            checkBox.setOnCheckedChangeListener(
                    (buttonView, isChecked) -> viewModel.getPresenter().onFoodCategorySelected(index, isChecked));
            checkboxContainer.addView(checkBox);
        }

        new AlertDialog.Builder(this)
                .setTitle("Select Categories")
                .setView(dialogView)
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