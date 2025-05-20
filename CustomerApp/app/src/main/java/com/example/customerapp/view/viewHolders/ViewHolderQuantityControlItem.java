package com.example.customerapp.view.viewHolders;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.customerapp.R;

public class ViewHolderQuantityControlItem extends RecyclerView.ViewHolder {
    public final TextView txtItem;
    public final Button btnIncrease;
    public final Button btnDecrease;
    public final TextView txtQuantity;

    public ViewHolderQuantityControlItem(View view) {
        super(view);
        txtItem = view.findViewById(R.id.txt_item);
        btnIncrease = view.findViewById(R.id.btn_increase);
        btnDecrease = view.findViewById(R.id.btn_decrease);
        txtQuantity = view.findViewById(R.id.txt_quantity);
    }
}

