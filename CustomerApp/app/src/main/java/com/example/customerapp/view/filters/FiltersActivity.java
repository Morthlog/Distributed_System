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

import java.util.Arrays;
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
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Price Categories");

        String[] priceArray = availablePrices.toArray(new String[0]);

        builder.setMultiChoiceItems(priceArray, checkedItems,
                (dialog, which, isChecked) ->
                {
                    viewModel.getPresenter().onPriceSelected(which, isChecked);
                });

        builder.setPositiveButton("OK",
                (dialog, which) ->
                {
                    viewModel.getPresenter().onConfirmPriceSelection();
                });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }


    public void showStarsDialog(int[] starOptions, int defaultIndex)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Stars");

        final int[] selected = {defaultIndex != -1 ? starOptions[defaultIndex] : 0};

        builder.setSingleChoiceItems
                (
                        Arrays.stream(starOptions).mapToObj(String::valueOf).toArray(String[]::new),
                        defaultIndex,
                        (dialog, which) -> selected[0] = starOptions[which]
                );

        builder.setPositiveButton("OK",
                (dialog, which) ->
                        viewModel.getPresenter().onConfirmStarsSelection(selected[0])
        );

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }


    @Override
    public void showFoodCategoryDialog(List<FoodCategory> availableCategories, boolean[] checkedItems)
    {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_checkboxes, null);
        LinearLayout checkboxContainer = dialogView.findViewById(R.id.checkboxContainer);

        for (int i = 0; i < availableCategories.size(); i++)
        {
            FoodCategory category = availableCategories.get(i);
            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(category.name());
            checkBox.setChecked(checkedItems[i]);

            final int index = i;
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) ->
            {
                viewModel.getPresenter().onFoodCategorySelected(index, isChecked);
            });

            checkboxContainer.addView(checkBox);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Categories")
                .setView(dialogView)
                .setPositiveButton("OK",
                        (dialog, which) ->
                        {
                            viewModel.getPresenter().onConfirmFoodCategorySelection();
                        })
                .setNegativeButton("Cancel", null)
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