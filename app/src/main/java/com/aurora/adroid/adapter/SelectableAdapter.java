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

import com.aurora.adroid.manager.RepoListManager;

import java.util.ArrayList;

abstract class SelectableAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {

    protected ArrayList<String> selections;
    protected Context context;
    protected RepoListManager repoListManager;

    SelectableAdapter(Context context) {
        this.context = context;
        repoListManager = new RepoListManager(context);
        ArrayList<String> selectedRepos = repoListManager.get();
        selections = new ArrayList<>();
        if (selectedRepos != null && !selectedRepos.isEmpty()) {
            selections.addAll(selectedRepos);
        }
    }

    boolean isSelected(String url) {
        return selections.contains(url);
    }

    void toggleSelection(int position) {
    }

    public void addSelectionsToRepoList() {
        repoListManager.addAll(selections);
    }

    public void removeSelectionsFromRepoList() {
        repoListManager.removeAll(selections);
        selections = new ArrayList<>();
    }
}
