package com.example.customerapp.view.shoppingCart;

import static com.example.customerapp.view.results.ResultsActivity.STORE_NAME_EXTRA;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.customerapp.R;
import com.example.customerapp.view.GenericRecyclerViewAdapter;
import com.example.customerapp.view.base.BaseActivity;
import com.example.customerapp.view.results.ResultsActivity;
import com.example.customerapp.view.viewHolders.ViewHolderQuantityControlItem;

import java.util.List;
import java.util.Locale;

import lib.shared.Product;

public class ShoppingCartActivity extends BaseActivity<ShoppingCartViewModel> implements ShoppingCartView
{
    GenericRecyclerViewAdapter<Product, ViewHolderQuantityControlItem> recyclerViewAdapter;
    Button buyButton;
    String storeName;

    TextView totalTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);
        viewModel.getPresenter().setView(this);
        if (savedInstanceState == null)
        {
            Intent intent = getIntent();
            storeName = intent.getStringExtra(STORE_NAME_EXTRA);
            viewModel.getPresenter().getStoreItems(storeName);
            viewModel.getPresenter().onSetStoreForCart(storeName);
        }
        TextView listName = findViewById(R.id.list_title);
        listName.setText(String.format("%s", storeName));

        totalTxt = findViewById(R.id.price_value_txt);

        buyButton = findViewById(R.id.filter_btn);
        buyButton.setText("buy");
        buyButton.setOnClickListener(v -> buyButtonClicked());
        setupBackPressed();
        updateTotal(0);
    }

    void setupBackPressed()
    {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true)
        {
            @Override
            public void handleOnBackPressed()
            {
                new AlertDialog.Builder(ShoppingCartActivity.this)
                        .setTitle("Cancel order")
                        .setMessage("Are you sure you want to cancel your order?")
                        .setPositiveButton("Yes", (dialog, which) -> finish())
                        .setNegativeButton("No", null)
                        .show();
            }
        });
    }

    private void buyButtonClicked()
    {
        buyButton.setClickable(false);
        viewModel.getPresenter().onBuyClicked();
    }


    @Override
    public void goToResultsActivity()
    {
        Intent intent = new Intent(this, ResultsActivity.class);

        startActivity(intent);
        finish();
    }

    @Override
    public void populateProductsRecyclerView(List<Product> products)
    {
        RecyclerView recyclerViewProducts = findViewById(R.id.recyclerView_List);
        recyclerViewProducts.setLayoutManager(new LinearLayoutManager(this));

        recyclerViewAdapter = new GenericRecyclerViewAdapter<>(
                products,
                (product, viewHolder) ->
                {
                    String productInfo = String.format(Locale.US, "%s: %.2f€", product.getProductName(), product.getPrice());
                    viewHolder.txtItem.setText(productInfo);
                    int quantity = viewModel.getPresenter().getQuantity(product.getProductName());
                    viewHolder.txtQuantity.setText(String.valueOf(quantity));
                    viewHolder.btnIncrease.setOnClickListener(v -> increaseQuantity(product));
                    viewHolder.btnDecrease.setOnClickListener(v -> decreaseQuantity(product));

                },
                (view) -> new ViewHolderQuantityControlItem(view),
                R.layout.list_item_quantity_control
        );

        recyclerViewProducts.setAdapter(recyclerViewAdapter);
    }

    @Override
    public void updateCartUI(int idx)
    {
        recyclerViewAdapter.notifyItemChanged(idx);
    }

    private void increaseQuantity(Product productName)
    {
        viewModel.getPresenter().onQuantityIncrease(productName);
    }

    private void decreaseQuantity(Product productName)
    {
        viewModel.getPresenter().onQuantityDecrease(productName);
    }

    private void showRatingDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.rating_bar, null);
        builder.setView(dialogView);

        RatingBar ratingBar = dialogView.findViewById(R.id.ratingBar);

        builder.setTitle("Rate your experience")
                .setNegativeButton("Skip",
                        (dialog, which) -> goToResultsActivity())
                .setPositiveButton("Rate",
                        (dialog, which) ->
                        {
                            int rating = (int) ratingBar.getRating();
                            viewModel.getPresenter().onRateStore(storeName, rating);
                        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showBuyVerificationDialog(String title, String message)
    {
        new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(R.string.ok, (dialog, which) -> showRatingDialog())
                .show();
    }

    @Override
    public void showBuyMessageAsync(String title, String message)
    {
        runOnUiThread(() ->
        {
            showBuyVerificationDialog(title, message);
        });
    }

    @Override
    public void showRatingMessageAsync(String title, String message)
    {
        runOnUiThread(() ->
        {
            showRateVerificationDialog(title, message);
        });
    }

    @Override
    public void updateTotal(double total)
    {
        String totalStr = String.format(Locale.US, "%.2f€", total);
        totalTxt.setText(totalStr);
    }

    private void showRateVerificationDialog(String title, String message)
    {
        new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(R.string.ok, (dialog, which) -> goToResultsActivity())
                .show();
    }

    @Override
    protected ShoppingCartViewModel createViewModel()
    {
        return new ViewModelProvider(this).get(ShoppingCartViewModel.class);
    }
}
