package com.aurora.adroid.model.items.cluster;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;

import com.aurora.adroid.GlideApp;
import com.aurora.adroid.R;
import com.aurora.adroid.model.App;
import com.aurora.adroid.model.Repo;
import com.aurora.adroid.util.DatabaseUtil;
import com.aurora.adroid.util.Util;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

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

        @BindView(R.id.img)
        ImageView img;
        @BindView(R.id.line1)
        AppCompatTextView line1;
        @BindView(R.id.line2)
        AppCompatTextView line2;
        @BindView(R.id.line3)
        AppCompatTextView line3;

        public ViewHolder(@NonNull View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        @Override
        public void bindView(@NotNull BaseClusterItem item, @NotNull List<?> list) {
            final Context context = itemView.getContext();
            final App app = item.getApp();

            line1.setText(app.getName());
            line2.setText(app.getRepoName());
            line3.setText(Util.getDateFromMilli(app.getAdded()));

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

        @Override
        public void unbindView(@NotNull BaseClusterItem item) {
            line1.setText(null);
            line2.setText(null);
            line3.setText(null);
            img.setImageDrawable(null);
        }
    }
}
