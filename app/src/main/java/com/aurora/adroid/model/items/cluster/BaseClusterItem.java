package com.aurora.adroid.model.items.cluster;

import android.view.View;

import com.aurora.adroid.R;
import com.aurora.adroid.model.App;
import com.mikepenz.fastadapter.items.AbstractItem;

import org.jetbrains.annotations.NotNull;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public abstract class BaseClusterItem extends AbstractItem<BaseViewHolder> {

    private App app;
    private String packageName;

    public BaseClusterItem(App app) {
        this.app = app;
        this.packageName = app.getPackageName();
    }

    @Override
    public int getLayoutRes() {
        return R.layout.item_cluster;
    }

    @NotNull
    @Override
    public abstract BaseViewHolder getViewHolder(@NotNull View view);

    @Override
    public int getType() {
        return R.id.fastadapter_item;
    }
}
