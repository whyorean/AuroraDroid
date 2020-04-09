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

package com.aurora.adroid.manager;

import android.content.Context;

import com.aurora.adroid.Constants;
import com.aurora.adroid.model.Repo;
import com.aurora.adroid.util.Log;
import com.aurora.adroid.util.PrefUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RepoListManager {

    private final HashMap<String, Repo> repoHashMap = new HashMap<>();

    private Context context;
    private Gson gson;

    public RepoListManager(Context context) {
        this.context = context;
        this.gson = new Gson();
        this.repoHashMap.putAll(getDefaultRepoMap());
    }

    public List<Repo> getAllRepoList() {
        return new ArrayList<>(repoHashMap.values());
    }

    public boolean addToRepoMap(Repo repo) {
        synchronized (repoHashMap) {
            if (repoHashMap.containsKey(repo.getRepoId())) {
                return false;
            } else {
                repoHashMap.put(repo.getRepoId(), repo);
                saveRepoMap();
                return true;
            }
        }
    }

    public Repo getRepoById(String repoId) {
        synchronized (repoHashMap) {
            if (repoHashMap.containsKey(repoId))
                return repoHashMap.get(repoId);
            else
                return new Repo();
        }
    }

    public void removeFromRepoMap(Repo repo) {
        synchronized (repoHashMap) {
            repoHashMap.remove(repo.getRepoId());
            saveRepoMap();
        }
    }

    private void saveRepoMap() {
        synchronized (repoHashMap) {
            PrefUtil.putString(context, Constants.PREFERENCE_DEFAULT_REPO_MAP, gson.toJson(repoHashMap));
        }
    }

    public void clear() {
        synchronized (repoHashMap) {
            repoHashMap.clear();
            saveRepoMap();
        }
    }

    private HashMap<String, Repo> getDefaultRepoMap() {
        final String rawList = PrefUtil.getString(context, Constants.PREFERENCE_DEFAULT_REPO_MAP);
        final Type type = new TypeToken<HashMap<String, Repo>>() {
        }.getType();
        final HashMap<String, Repo> repoHashMap = gson.fromJson(rawList, type);

        if (repoHashMap == null || repoHashMap.isEmpty())
            return getDefaultRepoMapFromAssets();
        else
            return repoHashMap;
    }

    private HashMap<String, Repo> getDefaultRepoMapFromAssets() {
        final HashMap<String, Repo> repoHashMap = new HashMap<>();
        try {
            final InputStream inputStream = context.getAssets().open("repo.json");
            final byte[] bytes = new byte[inputStream.available()];

            inputStream.read(bytes);
            inputStream.close();

            final String rawJSON = new String(bytes, StandardCharsets.UTF_8);
            final Type listType = new TypeToken<List<Repo>>() {
            }.getType();
            final List<Repo> repoList = gson.fromJson(rawJSON, listType);

            for (Repo repo : repoList)
                repoHashMap.put(repo.getRepoId(), repo);
        } catch (IOException e) {
            Log.e(e.getMessage());
        }
        return repoHashMap;
    }
}
