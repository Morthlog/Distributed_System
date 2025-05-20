package com.example.customerapp.view.results;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.customerapp.R;
import com.example.customerapp.view.GenericRecyclerViewAdapter;
import com.example.customerapp.view.base.BaseActivity;
import com.example.customerapp.view.filters.FiltersActivity;
import com.example.customerapp.view.shoppingCart.ShoppingCartActivity;
import com.example.customerapp.view.viewHolders.ViewHolderSingleTextViewImage;

import java.util.List;

import lib.shared.Store;

public class ResultsActivity extends BaseActivity<ResultsViewModel> implements ResultsView
{
    public static final String STORE_NAME_EXTRA = "store_name";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_list);
        viewModel.getPresenter().setView(this);

        TextView listTitle = findViewById(R.id.list_title);
        listTitle.setText(R.string.results);

        viewModel.getPresenter().searchStores();
        Button filterBtn = findViewById(R.id.filter_btn);
        filterBtn.setOnClickListener(v -> goToFiltersActivity());
    }

    @Override
    public void populateStoresRecyclerView(List<Store> stores)
    {
        RecyclerView recyclerViewMembers = findViewById(R.id.recyclerView_List);
        recyclerViewMembers.setLayoutManager(new LinearLayoutManager(this));

        GenericRecyclerViewAdapter<Store, ViewHolderSingleTextViewImage> recyclerViewAdapter = new GenericRecyclerViewAdapter<>(
                stores,
                (store, viewHolder) ->
                {
                    viewHolder.txtItem.setText(store.getStoreName());
                    viewHolder.txtItem.setOnClickListener(v -> selectStore(store));

                    Bitmap bitmap = BitmapFactory.decodeByteArray(store.getImage(), 0, store.getImage().length);
                    viewHolder.imageView.setImageBitmap(bitmap);
                },
                (view) -> new ViewHolderSingleTextViewImage(view, R.id.txt_item, R.id.img_item),
                R.layout.list_item_image_and_text
        );

        recyclerViewMembers.setAdapter(recyclerViewAdapter);
    }


    private void selectStore(Store store)
    {
        goToShoppingCartActivity(store.getStoreName());
    }

    @Override
    public void populateStoresRecyclerViewAsync(List<Store> stores)
    {
        runOnUiThread(() ->
        {
            populateStoresRecyclerView( stores);
        });
    }
    @Override
    protected ResultsViewModel createViewModel()
    {
        return new ViewModelProvider(this).get(ResultsViewModel.class);
    }


    public void goToShoppingCartActivity(String storeName)
    {
        Intent intent = new Intent(this, ShoppingCartActivity.class);
        intent.putExtra(STORE_NAME_EXTRA, storeName);
        startActivity(intent);
        finish();
    }

    @Override
    public void goToFiltersActivity()
    {
        Intent intent = new Intent(this, FiltersActivity.class);

        startActivity(intent);
        finish();
    }
    @Override
    public void showMessageAsync(String title, String message)
    {
        runOnUiThread(() ->
        {
            super.showMessage(title,message);
        });
    }
}
