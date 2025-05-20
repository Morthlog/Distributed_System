package com.example.customerapp.view.shoppingCart;

import static com.example.customerapp.view.results.ResultsActivity.STORE_NAME_EXTRA;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.customerapp.R;
import com.example.customerapp.view.GenericRecyclerViewAdapter;
import com.example.customerapp.view.base.BaseActivity;
import com.example.customerapp.view.results.ResultsActivity;
import com.example.customerapp.view.viewHolders.ViewHolderQuantityControlItem;

import java.util.List;

import lib.shared.Product;

public class ShoppingCartActivity extends BaseActivity<ShoppingCartViewModel> implements ShoppingCartView
{
    private TextView listName;
    GenericRecyclerViewAdapter<Product, ViewHolderQuantityControlItem> recyclerViewAdapter;
    Button buyButton;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_list);
        viewModel.getPresenter().setView(this);

        if (savedInstanceState == null)
        {
            Intent intent = getIntent();
            String storeName = intent.getStringExtra(STORE_NAME_EXTRA);
            viewModel.getPresenter().getStoreItems(storeName);
            viewModel.getPresenter().onSetStoreForCart(storeName);
        }
        listName = findViewById(R.id.list_title);
        listName.setText("Products");

        buyButton = findViewById(R.id.filter_btn);
        buyButton.setText("buy");
        buyButton.setOnClickListener(v -> buyButtonClicked());
    }

    private void showVerificationDialog(String title, String message)
    {
        new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(R.string.ok, (dialog, which) -> goToResultsActivity())
                .show();
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
        RecyclerView recyclerViewMembers = findViewById(R.id.recyclerView_List);
        recyclerViewMembers.setLayoutManager(new LinearLayoutManager(this));

        recyclerViewAdapter = new GenericRecyclerViewAdapter<>(
                products,
                (product, viewHolder) ->
                {
                    viewHolder.txtItem.setText(product.getProductName());
                    int quantity = viewModel.getPresenter().getQuantity(product.getProductName());
                    viewHolder.txtQuantity.setText(String.valueOf(quantity));
                    viewHolder.btnIncrease.setOnClickListener(v -> increaseQuantity(product));
                    viewHolder.btnDecrease.setOnClickListener(v -> decreaseQuantity(product));

                },
                (view) -> new ViewHolderQuantityControlItem(view),
                R.layout.list_item_quantity_control
        );

        recyclerViewMembers.setAdapter(recyclerViewAdapter);
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

    @Override
    public void showMessageAsync(String title, String message)
    {
        runOnUiThread(() ->
        {
            showVerificationDialog(title,message);
        });
    }
    @Override
    protected ShoppingCartViewModel createViewModel()
    {
        return new ViewModelProvider(this).get(ShoppingCartViewModel.class);
    }
}
