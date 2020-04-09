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
