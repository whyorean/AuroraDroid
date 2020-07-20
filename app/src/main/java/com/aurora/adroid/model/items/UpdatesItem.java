/*
 * Aurora Droid
 * Copyright (C) 2019-20, Rahul Kumar Patel <whyorean@gmail.com>
 *
 * Aurora Droid is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Aurora Droid is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Aurora Droid.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.aurora.adroid.model.items;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.recyclerview.widget.RecyclerView;

import com.aurora.adroid.GlideApp;
import com.aurora.adroid.R;
import com.aurora.adroid.model.App;
import com.aurora.adroid.util.ContextUtil;
import com.aurora.adroid.util.DatabaseUtil;
import com.aurora.adroid.util.LocalizationUtil;
import com.aurora.adroid.util.Util;
import com.aurora.adroid.util.ViewUtil;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;
import com.mikepenz.fastadapter.listeners.ClickEventHook;
import com.mikepenz.fastadapter.select.SelectExtension;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import lombok.Getter;
import lombok.Setter;

public class UpdatesItem extends AbstractItem<UpdatesItem.ViewHolder> {

    @Getter
    @Setter
    private App app;
    @Getter
    @Setter
    private String packageName;

    public UpdatesItem(App app) {
        this.app = app;
        this.packageName = app.getPackageName();
    }

    @Override
    public int getLayoutRes() {
        return R.layout.item_updates;
    }

    @NotNull
    @Override
    public ViewHolder getViewHolder(@NotNull View view) {
        return new ViewHolder(view);
    }

    @Override
    public int getType() {
        return 0;
    }

    public static class ViewHolder extends FastAdapter.ViewHolder<UpdatesItem> {

        @BindView(R.id.img)
        ImageView img;
        @BindView(R.id.line1)
        TextView line1;
        @BindView(R.id.line2)
        TextView line2;
        @BindView(R.id.line3)
        TextView line3;
        @BindView(R.id.txt_changes)
        TextView txtChanges;
        @BindView(R.id.layout_changes)
        RelativeLayout layoutChanges;
        @BindView(R.id.img_expand)
        ImageView imgExpand;
        @BindView(R.id.checkbox)
        MaterialCheckBox checkBox;

        private Context context;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            this.context = itemView.getContext();
        }

        @Override
        public void bindView(@NotNull UpdatesItem item, @NotNull List<?> list) {
            final App app = item.getApp();

            line1.setText(app.getName());
            line2.setText(StringUtils.joinWith(".", app.getPkg().getVersionName(), app.getPkg().getVersionCode()));
            line3.setText(StringUtils.joinWith("•", Util.humanReadableByteValue(app.getPkg().getSize(), true)));
            txtChanges.setText(LocalizationUtil.getLocalizedChangelog(context, app));

            if (app.getIcon() == null)
                img.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_placeholder));
            else
                GlideApp
                        .with(context)
                        .asBitmap()
                        .load(DatabaseUtil.getImageUrl(app))
                        .placeholder(R.drawable.ic_placeholder)
                        .addListener(new RequestListener<Bitmap>() {
                            @Override
                            public boolean onLoadFailed(@androidx.annotation.Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                                if (resource.getPixel(0, 0) != Color.TRANSPARENT) {
                                    RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(context.getResources(), resource);
                                    roundedBitmapDrawable.setCornerRadius(ViewUtil.pxToDp(context, 18));
                                    ContextUtil.runOnUiThread(() -> img.setImageDrawable(roundedBitmapDrawable));
                                } else {
                                    ContextUtil.runOnUiThread(() -> img.setImageBitmap(resource));
                                }
                                return false;
                            }
                        })
                        .submit();

            imgExpand.setOnClickListener(v -> {
                boolean isVisible = layoutChanges.getVisibility() == View.VISIBLE;
                if (isVisible) {
                    ViewUtil.collapse(layoutChanges);
                    ViewUtil.rotateView(imgExpand, true);
                } else {
                    ViewUtil.rotateView(imgExpand, false);
                    ViewUtil.expand(layoutChanges);
                }
            });

            checkBox.setChecked(item.isSelected());
        }

        @Override
        public void unbindView(@NotNull UpdatesItem item) {
            line1.setText(null);
            line2.setText(null);
            line3.setText(null);
            line3.setText(null);
            img.setImageDrawable(null);
            img.setImageBitmap(null);
            layoutChanges.setVisibility(View.GONE);
        }
    }

    public static final class CheckBoxClickEvent extends ClickEventHook<UpdatesItem> {
        @Nullable
        public View onBind(@NotNull RecyclerView.ViewHolder viewHolder) {
            return viewHolder instanceof UpdatesItem.ViewHolder
                    ? ((UpdatesItem.ViewHolder) viewHolder).checkBox
                    : null;
        }

        @Override
        public void onClick(@NotNull View view, int position, @NotNull FastAdapter<UpdatesItem> fastAdapter, @NotNull UpdatesItem item) {
            SelectExtension<UpdatesItem> selectExtension = fastAdapter.getExtension(SelectExtension.class);
            if (selectExtension != null) {
                selectExtension.toggleSelection(position);
            }
        }
    }
}
