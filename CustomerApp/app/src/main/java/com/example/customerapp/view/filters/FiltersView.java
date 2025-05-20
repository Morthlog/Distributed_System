package com.example.customerapp.view.filters;


import com.example.customerapp.view.base.BaseView;

import java.util.List;

import lib.shared.FoodCategory;

public interface FiltersView extends BaseView
{
    void showPriceDialog(List<String> availablePrices, boolean[] checkedItems);

    void showFoodCategoryDialog(List<FoodCategory> availableCategories, boolean[] checkedItems);

    void showStarsDialog(int[] starOptions, int defaultIndex);
}
