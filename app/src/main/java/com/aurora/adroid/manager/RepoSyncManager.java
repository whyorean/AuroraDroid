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
import com.aurora.adroid.model.StaticRepo;
import com.aurora.adroid.model.RepoHeader;
import com.aurora.adroid.task.DatabaseTask;
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

public class RepoSyncManager {

    private final HashMap<String, StaticRepo> repoHashMap = new HashMap<>();
    private final HashMap<String, StaticRepo> syncHashMap = new HashMap<>();
    private final HashMap<String, RepoHeader> headerHashMap = new HashMap<>();

    private Context context;
    private Gson gson;

    public RepoSyncManager(Context context) {
        this.context = context;
        this.gson = new Gson();
        this.repoHashMap.putAll(getRepoHashMap());
        this.syncHashMap.putAll(getSyncedHashMap());
        this.headerHashMap.putAll(getHeaderHashMap());
    }

    public void addToRepoMap(StaticRepo staticRepo) {
        synchronized (repoHashMap) {
            if (!repoHashMap.containsKey(staticRepo.getRepoId())) {
                repoHashMap.put(staticRepo.getRepoId(), staticRepo);
            }
        }
    }

    public void addDefault() {
        synchronized (repoHashMap) {
            final StaticRepo staticRepo = getDefaultFromAssets();
            if (staticRepo != null && !repoHashMap.containsKey(staticRepo.getRepoId())) {
                repoHashMap.put(staticRepo.getRepoId(), staticRepo);
                saveRepoMap();
            }
        }
    }

    public void addToSyncMap(StaticRepo staticRepo) {
        synchronized (syncHashMap) {
            if (!syncHashMap.containsKey(staticRepo.getRepoId())) {
                syncHashMap.put(staticRepo.getRepoId(), staticRepo);
            }
            saveSyncMap();
        }
    }

    public void addToHeaderMap(RepoHeader repoHeader) {
        synchronized (headerHashMap) {
            if (!headerHashMap.containsKey(repoHeader.getRepoId())) {
                headerHashMap.put(repoHeader.getRepoId(), repoHeader);
            }
            saveRepoHeaderMap();
        }
    }

    public void addAllToRepoMap(List<StaticRepo> staticRepoList) {
        synchronized (repoHashMap) {
            for (StaticRepo staticRepo : staticRepoList) {
                addToRepoMap(staticRepo);
            }
            saveRepoMap();
        }
    }

    public List<StaticRepo> getRepoList() {
        synchronized (repoHashMap) {
            return new ArrayList<>(repoHashMap.values());
        }
    }

    public List<StaticRepo> getSyncList() {
        synchronized (syncHashMap) {
            return new ArrayList<>(syncHashMap.values());
        }
    }

    public List<RepoHeader> getHeaderList() {
        synchronized (headerHashMap) {
            return new ArrayList<>(headerHashMap.values());
        }
    }

    public void updateRepoMap(List<StaticRepo> staticRepoList) {
        synchronized (repoHashMap) {
            clear();
            addAllToRepoMap(staticRepoList);
        }
    }

    public void updateSyncMap(List<StaticRepo> staticRepoList) {
        synchronized (syncHashMap) {
            final List<StaticRepo> syncedList = getSyncList();
            final DatabaseTask databaseTask = new DatabaseTask(context);
            for (StaticRepo staticRepo : syncedList) {
                if (!staticRepoList.contains(staticRepo)) {
                    syncHashMap.remove(staticRepo.getRepoId());
                    databaseTask.clearRepo(staticRepo);
                }
            }
            saveSyncMap();
        }
    }

    public void updateHeaderMap(List<StaticRepo> staticRepoList) {
        synchronized (headerHashMap) {
            final List<String> repoIdList = new ArrayList<>();
            final List<RepoHeader> syncedList = getHeaderList();

            for (StaticRepo staticRepo : staticRepoList)
                repoIdList.add(staticRepo.getRepoId());

            for (RepoHeader repoHeader : syncedList) {
                if (!repoIdList.contains(repoHeader.getRepoId())) {
                    headerHashMap.remove(repoHeader.getRepoId());
                }
            }
            saveRepoHeaderMap();
        }
    }

    public void removeFromRepoMap(StaticRepo staticRepo) {
        synchronized (repoHashMap) {
            repoHashMap.remove(staticRepo.getRepoId());
        }
    }

    public void removeFromSyncMap(StaticRepo staticRepo) {
        synchronized (syncHashMap) {
            syncHashMap.remove(staticRepo.getRepoId());
        }
    }

    public boolean isAdded(StaticRepo staticRepo) {
        synchronized (repoHashMap) {
            return repoHashMap.containsKey(staticRepo.getRepoId());
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
            saveRepoMap();
        }
    }

    private void saveRepoMap() {
        synchronized (repoHashMap) {
            PrefUtil.putString(context, Constants.PREFERENCE_REPO_MAP, gson.toJson(repoHashMap));
        }
    }

    private void saveSyncMap() {
        synchronized (syncHashMap) {
            PrefUtil.putString(context, Constants.PREFERENCE_SYNC_MAP, gson.toJson(syncHashMap));
        }
    }

    private void saveRepoHeaderMap() {
        synchronized (headerHashMap) {
            PrefUtil.putString(context, Constants.PREFERENCE_REPO_HEADER_MAP, gson.toJson(headerHashMap));
        }
    }

    private HashMap<String, StaticRepo> getRepoHashMap() {
        final String rawList = PrefUtil.getString(context, Constants.PREFERENCE_REPO_MAP);
        final Type type = new TypeToken<HashMap<String, StaticRepo>>() {
        }.getType();
        final HashMap<String, StaticRepo> repoList = gson.fromJson(rawList, type);

        if (repoList == null)
            return new HashMap<>();
        else
            return repoList;
    }

    private HashMap<String, StaticRepo> getSyncedHashMap() {
        final String rawList = PrefUtil.getString(context, Constants.PREFERENCE_SYNC_MAP);
        final Type type = new TypeToken<HashMap<String, StaticRepo>>() {
        }.getType();
        final HashMap<String, StaticRepo> repoList = gson.fromJson(rawList, type);

        if (repoList == null)
            return new HashMap<>();
        else
            return repoList;
    }

    private HashMap<String, RepoHeader> getHeaderHashMap() {
        final String jsonString = PrefUtil.getString(context, Constants.PREFERENCE_REPO_HEADER_MAP);
        final Type type = new TypeToken<HashMap<String, RepoHeader>>() {
        }.getType();
        final HashMap<String, RepoHeader> repoHeaderList = gson.fromJson(jsonString, type);

        if (repoHeaderList == null || repoHeaderList.isEmpty())
            return new HashMap<>();
        else
            return repoHeaderList;
    }

    private StaticRepo getDefaultFromAssets() {
        try {
            final InputStream inputStream = context.getAssets().open("default.json");
            final byte[] bytes = new byte[inputStream.available()];

            inputStream.read(bytes);
            inputStream.close();

            final String rawJSON = new String(bytes, StandardCharsets.UTF_8);
            return gson.fromJson(rawJSON, StaticRepo.class);
        } catch (IOException e) {
            Log.e(e.getMessage());
            return null;
        }
    }
}
