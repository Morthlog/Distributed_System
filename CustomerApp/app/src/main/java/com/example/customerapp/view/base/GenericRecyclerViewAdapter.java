package com.example.customerapp.view.base;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class GenericRecyclerViewAdapter<T, VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH>
{
    private final List<T> list;
    private final ItemBinder<T, VH> itemBinder;
    private final ViewHolderCreator<VH> viewHolderCreator;
    private final int itemLayout;

    public GenericRecyclerViewAdapter(List<T> list, ItemBinder<T, VH> itemBinder, ViewHolderCreator<VH> viewHolderCreator, int itemLayout)
    {
        this.list = list;
        this.itemBinder = itemBinder;
        this.viewHolderCreator = viewHolderCreator;
        this.itemLayout = itemLayout;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType)
    {
        return viewHolderCreator.create(LayoutInflater.from(parent.getContext()).inflate(itemLayout, parent, false));
    }

    public void updateList(int removedIndex)
    {
        list.remove(removedIndex);
        notifyItemRemoved(removedIndex);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position)
    {
        final T currentObject = list.get(position);
        itemBinder.bindData(currentObject, holder);
    }

    @Override
    public int getItemCount()
    {
        return list.size();
    }

    public interface ItemBinder<T, VH extends RecyclerView.ViewHolder>
    {
        void bindData(T item, VH viewHolder);
    }

    public interface ViewHolderCreator<VH extends RecyclerView.ViewHolder>
    {
        VH create(View view);
    }
}
