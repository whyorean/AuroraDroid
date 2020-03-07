/*
 * Aurora Store
 * Copyright (C) 2019, Rahul Kumar Patel <whyorean@gmail.com>
 *
 * Aurora Store is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * Aurora Store is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Aurora Store.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package com.aurora.adroid.manager;

import android.content.Context;

import com.aurora.adroid.Constants;
import com.aurora.adroid.model.Repo;
import com.aurora.adroid.util.PrefUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class RepoManager {

    private Context context;
    private Gson gson;

    public RepoManager(Context context) {
        this.context = context;
        this.gson = new Gson();
    }

    public void addToRepoList(Repo repo) {
        List<Repo> repoList = getRepoList();
        if (!repoList.contains(repo)) {
            repoList.add(repo);
            saveRepoList(repoList);
        }
    }

    public void addAllToRepoList(List<Repo> newRepoList) {
        List<Repo> stringList = getRepoList();
        for (Repo repo : newRepoList) {
            if (!stringList.contains(repo)) {
                stringList.add(repo);
                saveRepoList(stringList);
            }
        }
    }

    public void removeFromRepoList(Repo repo) {
        List<Repo> repoList = getRepoList();
        if (repoList.contains(repo)) {
            repoList.remove(repo);
            saveRepoList(repoList);
        }
    }

    public boolean isAdded(Repo repo) {
        return getRepoList().contains(repo);
    }

    public void clear() {
        saveRepoList(new ArrayList<>());
    }

    private void saveRepoList(List<Repo> stringList) {
        PrefUtil.putString(context, Constants.PREFERENCE_REPO_LIST, gson.toJson(stringList));
    }

    public List<Repo> getRepoList() {
        String rawList = PrefUtil.getString(context, Constants.PREFERENCE_REPO_LIST);
        Type type = new TypeToken<List<Repo>>() {
        }.getType();
        List<Repo> repoList = gson.fromJson(rawList, type);

        if (repoList == null)
            return new ArrayList<>();
        else
            return repoList;
    }
}
