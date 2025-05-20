package com.example.customerapp.view.viewHolders;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

public class ViewHolderSingleTextViewImage extends RecyclerView.ViewHolder
{
    public final TextView txtItem;
    public final ImageView imageView;

    public ViewHolderSingleTextViewImage(View view, int txtId, int imgId)
    {
        super(view);
        this.txtItem = view.findViewById(txtId);
        this.imageView = view.findViewById(imgId);
    }
}

