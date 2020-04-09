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

import android.view.View;

import com.mikepenz.fastadapter.FastAdapter;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class BaseViewHolder extends FastAdapter.ViewHolder<BaseClusterItem> {


    public BaseViewHolder(@NotNull View itemView) {
        super(itemView);
    }

    @Override
    public abstract void bindView(@NotNull BaseClusterItem item, @NotNull List<?> list);

    @Override
    public abstract void unbindView(@NotNull BaseClusterItem item);
}
