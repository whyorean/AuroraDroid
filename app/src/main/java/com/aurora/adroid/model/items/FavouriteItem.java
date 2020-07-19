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
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aurora.adroid.GlideApp;
import com.aurora.adroid.R;
import com.aurora.adroid.model.App;
import com.aurora.adroid.util.DatabaseUtil;
import com.aurora.adroid.util.PackageUtil;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;
import com.mikepenz.fastadapter.listeners.ClickEventHook;
import com.mikepenz.fastadapter.select.SelectExtension;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FavouriteItem extends AbstractItem<FavouriteItem.ViewHolder> {
    private App app;
    private String packageName;

    public FavouriteItem(App app) {
        this.app = app;
        this.packageName = app.getPackageName();
    }

    @Override
    public int getLayoutRes() {
        return R.layout.item_checkbox;
    }

    @NotNull
    @Override
    public ViewHolder getViewHolder(@NotNull View view) {
        return new ViewHolder(view);
    }

    @Override
    public int getType() {
        return R.id.fastadapter_item;
    }

    public static class ViewHolder extends FastAdapter.ViewHolder<FavouriteItem> {
        @BindView(R.id.img)
        ImageView img;
        @BindView(R.id.line1)
        TextView line1;
        @BindView(R.id.line2)
        TextView line2;
        @BindView(R.id.checkbox)
        MaterialCheckBox checkBox;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @Override
        public void bindView(@NotNull FavouriteItem item, @NotNull List<?> list) {
            final App app = item.getApp();
            final Context context = itemView.getContext();

            line1.setText(app.getName());
            line2.setText(context.getText(PackageUtil.isInstalled(context, app.getPackageName())
                    ? R.string.list_installed
                    : R.string.list_not_installed));

            checkBox.setChecked(item.isSelected());

            if (app.isInstalled()){
                checkBox.setChecked(true);
                checkBox.setEnabled(false);
            }

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
        public void unbindView(@NotNull FavouriteItem item) {
            line1.setText(null);
            line2.setText(null);
        }
    }

    public static final class CheckBoxClickEvent extends ClickEventHook<FavouriteItem> {
        @Nullable
        public View onBind(@NotNull RecyclerView.ViewHolder viewHolder) {
            return viewHolder instanceof ViewHolder
                    ? ((ViewHolder) viewHolder).checkBox
                    : null;
        }

        @Override
        public void onClick(@NotNull View view, int position, @NotNull FastAdapter<FavouriteItem> fastAdapter, @NotNull FavouriteItem item) {
            SelectExtension<FavouriteItem> selectExtension = fastAdapter.getExtension(SelectExtension.class);
            if (selectExtension != null) {
                selectExtension.toggleSelection(position);
            }
        }
    }
}
