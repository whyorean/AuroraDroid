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

import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

import com.aurora.adroid.R;
import com.aurora.adroid.model.v2.Repo;
import com.aurora.adroid.util.Util;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import lombok.Getter;

public class RepoItem extends AbstractItem<RepoItem.RepoItemHolder> {

    @Getter
    private Repo index;

    public RepoItem(Repo index) {
        this.index = index;
    }

    @NotNull
    @Override
    public RepoItemHolder getViewHolder(@NotNull View view) {
        return new RepoItemHolder(view);
    }

    @Override
    public int getLayoutRes() {
        return R.layout.item_index;
    }

    @Override
    public int getType() {
        return R.id.fastadapter_item;
    }

    public static class RepoItemHolder extends FastAdapter.ViewHolder<RepoItem> {
        @BindView(R.id.img)
        AppCompatImageView img;
        @BindView(R.id.line1)
        AppCompatTextView line1;
        @BindView(R.id.line2)
        AppCompatTextView line2;

        private Context context;

        RepoItemHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            this.context = view.getContext();
        }

        @Override
        public void bindView(@NotNull RepoItem item, @NotNull List<?> list) {
            Repo index = item.getIndex();
            line1.setText(index.getName().contains(" ") ? index.getName().split(" ")[0] : index.getName());
            line2.setText(Util.getDateFromMilli(index.getTimestamp()));
            img.setImageDrawable(context.getDrawable(R.drawable.ic_repo_alt));
        }

        @Override
        public void unbindView(@NotNull RepoItem item) {
            line1.setText(null);
            line2.setText(null);
            img.setImageDrawable(null);
        }
    }
}
