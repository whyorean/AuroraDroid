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

package com.aurora.adroid.task;

import android.content.Context;
import android.content.ContextWrapper;
import android.database.sqlite.SQLiteConstraintException;

import com.aurora.adroid.ArchType;
import com.aurora.adroid.database.AppDao;
import com.aurora.adroid.database.AppDatabase;
import com.aurora.adroid.database.PackageDao;
import com.aurora.adroid.manager.RepoListManager;
import com.aurora.adroid.model.App;
import com.aurora.adroid.model.Package;
import com.aurora.adroid.model.Repo;
import com.aurora.adroid.util.PackageUtil;
import com.google.gson.Gson;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class JsonParserTask extends ContextWrapper {

    private Context context;

    public JsonParserTask(Context context) {
        super(context);
        this.context = context;
    }

    public synchronized boolean parse(InputStream inputStream, String repoId) {
        final Gson gson = new Gson();
        final Repo repo = RepoListManager.getRepoById(context, repoId);
        final AppDatabase appDatabase = AppDatabase.getAppDatabase(context);
        final AppDao appDao = appDatabase.appDao();
        final PackageDao packageDao = appDatabase.packageDao();
        try {
            final String jsonString = IOUtils.toString(inputStream, "UTF-8");
            final JSONObject jsonObject = new JSONObject(jsonString);
            final JSONArray jsonArrayApp = jsonObject.getJSONArray("apps");

            for (int i = 0; i < jsonArrayApp.length(); i++) {
                final JSONObject appObj = jsonArrayApp.getJSONObject(i);
                final App app = gson.fromJson(appObj.toString(), App.class);
                app.setRepoId(repo.getRepoId());
                app.setRepoName(repo.getRepoName());
                app.setRepoUrl(repo.getRepoUrl());
                appDao.insert(app);
            }

            final JSONObject jsonArrayPackage = jsonObject.getJSONObject("packages");

            final Iterator<String> packageIterator = jsonArrayPackage.keys();
            List<String> packageList = new ArrayList<>();
            while (packageIterator.hasNext()) {
                packageList.add(packageIterator.next());
            }

            for (int i = 0; i < jsonArrayPackage.length(); i++) {
                final JSONArray jsonArray = jsonArrayPackage.getJSONArray(packageList.get(i));
                for (int index = 0; index < jsonArray.length(); index++) {
                    final JSONObject packageObj = jsonArray.getJSONObject(index);
                    final Package pkg = gson.fromJson(packageObj.toString(), Package.class);
                    pkg.setRepoName(repo.getRepoName());
                    pkg.setRepoUrl(repo.getRepoUrl());
                    packageDao.insert(pkg);
                }
            }
            return true;
        } catch (JSONException | IOException e) {
            e.printStackTrace();
            return false;
        } catch (SQLiteConstraintException e) {
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
