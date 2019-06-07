/*
 * Aurora Droid
 * Copyright (C) 2019, Rahul Kumar Patel <whyorean@gmail.com>
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
 */

package com.aurora.adroid.adapter;

import android.content.Context;

import androidx.recyclerview.widget.RecyclerView;

import com.aurora.adroid.manager.BlacklistManager;

import java.util.ArrayList;

abstract class BlacklistSelectableAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {

    protected ArrayList<String> selections;
    protected Context context;
    protected BlacklistManager mBlacklistManager;

    BlacklistSelectableAdapter(Context context) {
        this.context = context;
        mBlacklistManager = new BlacklistManager(context);
        ArrayList<String> blacklistedApps = mBlacklistManager.get();
        selections = new ArrayList<>();
        if (blacklistedApps != null && !blacklistedApps.isEmpty()) {
            selections.addAll(blacklistedApps);
        }
    }

    boolean isSelected(String packageName) {
        return selections.contains(packageName);
    }

    void toggleSelection(int position) {
    }

    public void addSelectionsToBlackList() {
        mBlacklistManager.addAll(selections);
    }

    public void removeSelectionsFromBlackList() {
        mBlacklistManager.removeAll(selections);
        selections = new ArrayList<>();
    }
}
