package com.aurora.adroid.model.items.cluster;

import android.view.View;

import androidx.annotation.NonNull;

import com.aurora.adroid.GlideApp;
import com.aurora.adroid.R;
import com.aurora.adroid.model.App;
import com.aurora.adroid.util.DatabaseUtil;
import com.aurora.adroid.util.Util;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class NewClusterItem extends BaseClusterItem {

    public NewClusterItem(App app) {
        super(app);
    }

    @NotNull
    @Override
    public ViewHolder getViewHolder(@NotNull View view) {
        return new ViewHolder(view);
    }


    public static class ViewHolder extends BaseViewHolder {

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        @Override
        public void bindView(@NotNull BaseClusterItem item, @NotNull List<?> list) {
            final App app = item.getApp();

            line1.setText(app.getName());
            line2.setText(Util.getDateFromMilli(app.getAdded()));

            if (app.getIcon() == null)
                img.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_placeholder));
            else
                GlideApp
                        .with(context)
                        .asBitmap()
                        .load(DatabaseUtil.getImageUrl(app))
                        .placeholder(R.drawable.ic_placeholder)
                        .into(img);
        }
    }
}
