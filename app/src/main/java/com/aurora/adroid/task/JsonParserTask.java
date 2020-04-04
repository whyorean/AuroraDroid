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
import android.database.sqlite.SQLiteException;

import com.aurora.adroid.database.AppDao;
import com.aurora.adroid.database.AppDatabase;
import com.aurora.adroid.database.IndexDao;
import com.aurora.adroid.database.PackageDao;
import com.aurora.adroid.manager.RepoBundle;
import com.aurora.adroid.manager.RepoListManager;
import com.aurora.adroid.model.App;
import com.aurora.adroid.model.Index;
import com.aurora.adroid.model.Package;
import com.aurora.adroid.model.Repo;
import com.aurora.adroid.util.Log;
import com.aurora.adroid.util.PathUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static com.aurora.adroid.Constants.JSON;

public class JsonParserTask extends ContextWrapper {

    private File file;
    private String repoDir;

    public JsonParserTask(Context context, File file) {
        super(context);
        this.file = file;
        this.repoDir = PathUtil.getRepoDirectory(context);
    }

    public RepoBundle parse() {

        boolean status = false;
        final AppDatabase appDatabase = AppDatabase.getDatabase(this);
        final AppDao appDao = appDatabase.appDao();
        final PackageDao packageDao = appDatabase.packageDao();
        final IndexDao indexDao = appDatabase.indexDao();

        final Repo repo = RepoListManager.getRepoById(this, FilenameUtils.getBaseName(file.getName()));

        final File jsonFile = new File(repoDir + repo.getRepoId() + JSON);
        final Gson gson = new GsonBuilder().excludeFieldsWithModifiers(Modifier.TRANSIENT).create();

        try {
            final String jsonString = IOUtils.toString(FileUtils.openInputStream(jsonFile), StandardCharsets.UTF_8);
            final JSONObject jsonObject = new JSONObject(jsonString);

            final JSONArray jsonArrayApp = jsonObject.getJSONArray("apps");
            final JSONObject jsonObjectIndex = jsonObject.getJSONObject("repo");
            final JSONObject jsonArrayPackage = jsonObject.getJSONObject("packages");

            final Index indexRepo = gson.fromJson(jsonObjectIndex.toString(), Index.class);
            indexRepo.setRepoId(repo.getRepoId());
            indexDao.insert(indexRepo);

            final List<App> appList = new ArrayList<>();
            final List<Package> packageList = new ArrayList<>();

            for (int i = 0; i < jsonArrayApp.length(); i++) {
                final JSONObject appObj = jsonArrayApp.getJSONObject(i);
                final App app = gson.fromJson(appObj.toString(), App.class);
                app.setRepoId(repo.getRepoId());
                app.setRepoName(repo.getRepoName());
                app.setRepoUrl(repo.getRepoUrl());
                appList.add(app);

                final JSONArray jsonArray = jsonArrayPackage.getJSONArray(app.getPackageName());
                for (int j = 0; j < jsonArray.length(); j++) {
                    final JSONObject packageObj = jsonArray.getJSONObject(j);
                    final Package pkg = gson.fromJson(packageObj.toString(), Package.class);
                    pkg.setRepoName(repo.getRepoName());
                    pkg.setRepoUrl(repo.getRepoUrl());
                    packageList.add(pkg);
                }
            }

            appDatabase.getQueryExecutor().execute(() -> {
                appDao.insertAll(appList);
                packageDao.insertAll(packageList);
            });

            status = true;
        } catch (JSONException e) {
            Log.e("Error processing JSON : %s", jsonFile.getName());
        } catch (FileNotFoundException e) {
            Log.e("File not found : %s", jsonFile.getName());
        } catch (IOException | SQLiteException e) {
            Log.e(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new RepoBundle(status, repo);
    }
}
