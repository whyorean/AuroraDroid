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
import com.aurora.adroid.task.DatabaseTask;
import com.aurora.adroid.util.PrefUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RepoSyncManager {

    private final HashMap<String, Repo> repoHashMap = new HashMap<>();
    private final HashMap<String, Repo> syncHashMap = new HashMap<>();

    private Context context;
    private Gson gson;

    public RepoSyncManager(Context context) {
        this.context = context;
        this.gson = new Gson();
        this.repoHashMap.putAll(getRepoHashMap());
        this.syncHashMap.putAll(getSyncedHashMap());
    }

    public void addToRepoMap(Repo repo) {
        synchronized (repoHashMap) {
            if (!repoHashMap.containsKey(repo.getRepoId())) {
                repoHashMap.put(repo.getRepoId(), repo);
            }
        }
    }

    public void addToSyncMap(Repo repo) {
        synchronized (syncHashMap) {
            if (!syncHashMap.containsKey(repo.getRepoId())) {
                syncHashMap.put(repo.getRepoId(), repo);
            }
            saveSyncList();
        }
    }

    public void addAllToRepoMap(List<Repo> repoList) {
        synchronized (repoHashMap) {
            for (Repo repo : repoList) {
                addToRepoMap(repo);
            }
            saveRepoList();
        }
    }

    public List<Repo> getRepoList() {
        synchronized (repoHashMap) {
            return new ArrayList<>(repoHashMap.values());
        }
    }

    public List<Repo> getSyncList() {
        synchronized (syncHashMap) {
            return new ArrayList<>(syncHashMap.values());
        }
    }

    public void updateRepoMap(List<Repo> repoList) {
        synchronized (repoHashMap) {
            clear();
            addAllToRepoMap(repoList);
        }
    }

    public void updateSyncMap(List<Repo> repoList) {
        synchronized (syncHashMap) {
            final List<Repo> syncedList = getSyncList();
            final DatabaseTask databaseTask = new DatabaseTask(context);
            for (Repo repo : syncedList) {
                if (!repoList.contains(repo)) {
                    syncHashMap.remove(repo.getRepoId());
                    databaseTask.clearRepo(repo.getRepoId());
                }
            }
            saveSyncList();
        }
    }

    public void removeFromRepoMap(Repo repo) {
        synchronized (repoHashMap) {
            repoHashMap.remove(repo.getRepoId());
        }
    }

    public void removeFromSyncMap(Repo repo) {
        synchronized (syncHashMap) {
            syncHashMap.remove(repo.getRepoId());
        }
    }

    public boolean isAdded(Repo repo) {
        synchronized (repoHashMap) {
            return repoHashMap.containsKey(repo.getRepoId());
        }
    }

    public boolean isSynced(String repoId) {
        synchronized (syncHashMap) {
            return syncHashMap.containsKey(repoId);
        }
    }

    public void clear() {
        synchronized (repoHashMap) {
            repoHashMap.clear();
            saveRepoList();
        }
    }

    private void saveRepoList() {
        synchronized (repoHashMap) {
            PrefUtil.putString(context, Constants.PREFERENCE_REPO_MAP, gson.toJson(repoHashMap));
        }
    }

    private void saveSyncList() {
        synchronized (syncHashMap) {
            PrefUtil.putString(context, Constants.PREFERENCE_SYNC_MAP, gson.toJson(syncHashMap));
        }
    }

    private HashMap<String, Repo> getRepoHashMap() {
        String rawList = PrefUtil.getString(context, Constants.PREFERENCE_REPO_MAP);
        Type type = new TypeToken<HashMap<String, Repo>>() {
        }.getType();
        HashMap<String, Repo> repoList = gson.fromJson(rawList, type);

        if (repoList == null)
            return new HashMap<>();
        else
            return repoList;
    }

    private HashMap<String, Repo> getSyncedHashMap() {
        String rawList = PrefUtil.getString(context, Constants.PREFERENCE_SYNC_MAP);
        Type type = new TypeToken<HashMap<String, Repo>>() {
        }.getType();
        HashMap<String, Repo> repoList = gson.fromJson(rawList, type);

        if (repoList == null)
            return new HashMap<>();
        else
            return repoList;
    }
}
