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

package com.aurora.adroid.manager;

import android.content.Context;

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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class RepoListManager {

    private static final String REPO_LIST = "REPO_LIST";
    private static final String CUSTOM_REPO_LIST = "CUSTOM_REPO_LIST";

    private Context context;
    private ArrayList<String> repoList;

    public RepoListManager(Context context) {
        this.context = context;
        repoList = PrefUtil.getListString(context, REPO_LIST);
    }

    public synchronized static void addRepoToCustomList(Context context, Repo repo) {
        Gson gson = new Gson();
        List<Repo> repoList = getCustomRepoList(context);
        //Remove Old copy & add new if already present
        Iterator<Repo> iterator = repoList.iterator();
        while (iterator.hasNext()) {
            Repo tempRepo = iterator.next();
            if (tempRepo.getRepoUrl().equals(repo.getRepoUrl()))
                iterator.remove();
        }
        repoList.add(repo);
        String json = gson.toJson(repoList);
        PrefUtil.putString(context, CUSTOM_REPO_LIST, json);
    }

    public synchronized static void removeRepoFromCustomList(Context context, Repo repo) {
        Gson gson = new Gson();
        List<Repo> repoList = getCustomRepoList(context);
        Iterator<Repo> iterator = repoList.iterator();
        while (iterator.hasNext()) {
            Repo tempRepo = iterator.next();
            if (tempRepo.getRepoUrl().equals(repo.getRepoUrl()))
                iterator.remove();
        }
        String json = gson.toJson(repoList);
        PrefUtil.putString(context, CUSTOM_REPO_LIST, json);
    }

    private static List<Repo> getCustomRepoList(Context context) {
        Gson gson = new Gson();
        Type type = new TypeToken<List<Repo>>() {
        }.getType();
        String json = PrefUtil.getString(context, CUSTOM_REPO_LIST);
        if (gson.fromJson(json, type) == null)
            return new ArrayList<>();
        else
            return gson.fromJson(json, type);
    }

    public static List<Repo> getAllRepoList(Context context) {
        List<Repo> repoList = getDefaultRepoList(context);
        repoList.addAll(RepoListManager.getCustomRepoList(context));
        return repoList;
    }

    private static List<Repo> getDefaultRepoList(Context context) {
        List<Repo> repoList = new ArrayList<>();
        try {
            InputStream inputStream = context.getAssets().open("repo.json");
            byte[] mByte = new byte[inputStream.available()];
            inputStream.read(mByte);
            inputStream.close();
            Gson gson = new Gson();
            String jsonOutput = new String(mByte, StandardCharsets.UTF_8);
            Type listType = new TypeToken<List<Repo>>() {
            }.getType();
            repoList = gson.fromJson(jsonOutput, listType);
        } catch (IOException e) {
            Log.i(e.getMessage());
        }
        return repoList;
    }

    public static List<Repo> getSelectedRepos(Context context) {
        List<Repo> repoList = new ArrayList<>();
        List<String> savedList = PrefUtil.getListString(context, REPO_LIST);
        for (Repo repo : getAllRepoList(context)) {
            if (savedList.contains(repo.getRepoId()))
                repoList.add(repo);
        }
        return repoList;
    }

    public static Repo getRepoById(Context context, String repoId) {
        Repo tempRepo = new Repo();
        for (Repo repo : getAllRepoList(context)) {
            if (repo.getRepoId().equals(repoId))
                tempRepo = repo;
        }
        return tempRepo;
    }

    public boolean add(String s) {
        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add(s);
        boolean result = addAll(arrayList);
        save();
        return result;
    }

    public boolean addAll(ArrayList<String> arrayList) {
        repoList.clear();
        boolean result = repoList.addAll(arrayList);
        Set<String> repoSet = new HashSet<>(repoList);
        repoList.clear();
        repoList.addAll(repoSet);
        save();
        return result;
    }

    public ArrayList<String> get() {
        return repoList;
    }

    public int getRepoCount() {
        return repoList.size();
    }

    public boolean contains(String url) {
        return repoList.contains(url);
    }

    public void remove(String url) {
        boolean success = repoList.remove(url);
        if (success)
            save();
    }

    public void removeAll(ArrayList<String> urlList) {
        boolean success = repoList.removeAll(urlList);
        if (success)
            save();
    }

    private void save() {
        PrefUtil.putListString(context, REPO_LIST, repoList);
    }
}
