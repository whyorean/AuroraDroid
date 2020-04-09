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

package com.aurora.adroid.util.diff;


import com.aurora.adroid.model.items.DownloadItem;
import com.mikepenz.fastadapter.diff.DiffCallback;

import org.jetbrains.annotations.Nullable;

public class DownloadDiffCallback implements DiffCallback<DownloadItem> {

    @Override
    public boolean areContentsTheSame(DownloadItem oldItem, DownloadItem newItem) {
        return oldItem.equals(newItem);
    }

    @Override
    public boolean areItemsTheSame(DownloadItem oldItem, DownloadItem newItem) {
        return oldItem.getDownload().getProgress() == newItem.getDownload().getProgress();
    }

    @Nullable
    @Override
    public Object getChangePayload(DownloadItem oldItem, int oldPosition, DownloadItem newItem, int newPosition) {
        return null;
    }
}