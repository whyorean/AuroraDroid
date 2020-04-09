package com.aurora.adroid.model.items.cluster;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;

import com.aurora.adroid.GlideApp;
import com.aurora.adroid.R;
import com.aurora.adroid.model.App;
import com.aurora.adroid.util.DatabaseUtil;
import com.aurora.adroid.util.Util;
import com.mikepenz.fastadapter.FastAdapter;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public abstract class BaseViewHolder extends FastAdapter.ViewHolder<BaseClusterItem> {


    public BaseViewHolder(@NotNull View itemView) {
        super(itemView);
    }

    @Override
    public abstract void bindView(@NotNull BaseClusterItem item, @NotNull List<?> list) ;

    @Override
    public abstract void unbindView(@NotNull BaseClusterItem item) ;
}
