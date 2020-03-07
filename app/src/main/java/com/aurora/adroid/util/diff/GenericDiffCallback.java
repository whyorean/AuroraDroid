package com.aurora.adroid.util.diff;

import com.aurora.adroid.model.items.GenericItem;
import com.mikepenz.fastadapter.diff.DiffCallback;

import org.jetbrains.annotations.Nullable;

public class GenericDiffCallback implements DiffCallback<GenericItem> {

    @Override
    public boolean areContentsTheSame(GenericItem oldItem, GenericItem newItem) {
        return oldItem.getApp().getPackageName().equals(newItem.getApp().getPackageName());
    }

    @Override
    public boolean areItemsTheSame(GenericItem oldItem, GenericItem newItem) {
        return oldItem.getApp().getPackageName().equals(newItem.getApp().getPackageName());
    }

    @Nullable
    @Override
    public Object getChangePayload(GenericItem oldItem, int oldPosition, GenericItem newItem, int newPosition) {
        return null;
    }
}