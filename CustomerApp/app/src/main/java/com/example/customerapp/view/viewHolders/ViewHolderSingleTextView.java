package com.example.customerapp.view.viewHolders;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

public class ViewHolderSingleTextView extends RecyclerView.ViewHolder
{
    public final TextView txtItem;
    public ViewHolderSingleTextView(View view, int id)
    {
        super(view);
        txtItem = view.findViewById(id);
    }
}
