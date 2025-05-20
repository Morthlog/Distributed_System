package com.example.customerapp.view.filters;


import com.example.customerapp.view.base.BasePresenter;
import com.example.customerapp.view.dao.CustomerDAO;
import com.example.customerapp.view.memorydao.FilterDAOMemory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lib.shared.Filter;
import lib.shared.FoodCategory;

public class FiltersPresenter extends BasePresenter<FiltersView>
{
    private List<String> selectedPrices;
    private List<String> availablePrices;

    private int[] availableStars;
    private int selectedStars;

    private List<FoodCategory> availableCategories;
    private List<FoodCategory> selectedCategories;
    Filter filter;

    public FiltersPresenter()
    {
        filter = FilterDAOMemory.getFilter();

        this.availablePrices = filter.getAvailablePrices();
        this.selectedPrices = filter.getPriceCategories();

        this.availableStars = filter.getAvailableStars();
        this.selectedStars = filter.getStars();

        this.availableCategories = filter.getAvailableFoodCategories();
        this.selectedCategories = filter.getFoodCategories();
    }

    public void onShowPriceDialog()
    {
        boolean[] checkedItems = new boolean[availablePrices.size()];
        for (int i = 0; i < availablePrices.size(); i++)
        {
            checkedItems[i] = selectedPrices.contains(availablePrices.get(i));
        }
        view.showPriceDialog(availablePrices, checkedItems);
    }

    public void onPriceSelected(int index, boolean isChecked)
    {
        String item = availablePrices.get(index);
        if (isChecked && !selectedPrices.contains(item))
        {
            selectedPrices.add(item);
        }
        else if (!isChecked)
        {
            selectedPrices.remove(item);
        }
    }

    public void onConfirmPriceSelection()
    {
        FilterDAOMemory.getFilter().setPriceCategories(selectedPrices);
    }

    public void onShowStarsDialog()
    {
        int defaultIndex = -1;
        for (int i = 0; i < availableStars.length; i++)
        {
            if (availableStars[i] == selectedStars)
            {
                defaultIndex = i;
                break;
            }
        }

        view.showStarsDialog(availableStars, defaultIndex);
    }

    public void onConfirmStarsSelection(int stars)
    {
        selectedStars = stars;
        FilterDAOMemory.getFilter().setStars(stars);
    }

    public void onShowFoodCategoryDialog()
    {
        boolean[] checkedItems = new boolean[availableCategories.size()];
        for (int i = 0; i < availableCategories.size(); i++)
        {
            checkedItems[i] = selectedCategories.contains(availableCategories.get(i));
        }
        view.showFoodCategoryDialog(availableCategories, checkedItems);
    }

    public void onFoodCategorySelected(int index, boolean isChecked)
    {
        FoodCategory category = availableCategories.get(index);
        if (isChecked && !selectedCategories.contains(category))
        {
            selectedCategories.add(category);
        }
        else if (!isChecked)
        {
            selectedCategories.remove(category);
        }
    }

    public void onConfirmFoodCategorySelection()
    {
        FilterDAOMemory.getFilter().setFoodCategories(selectedCategories);
    }
}
