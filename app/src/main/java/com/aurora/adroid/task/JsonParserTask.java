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

import com.aurora.adroid.database.AppDao;
import com.aurora.adroid.database.AppDatabase;
import com.aurora.adroid.database.PackageDao;
import com.aurora.adroid.manager.RepoBundle;
import com.aurora.adroid.manager.RepoListManager;
import com.aurora.adroid.model.App;
import com.aurora.adroid.model.Package;
import com.aurora.adroid.model.Repo;
import com.aurora.adroid.util.PathUtil;
import com.google.gson.Gson;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.reactivex.Observable;

import static com.aurora.adroid.Constants.JSON;

public class JsonParserTask extends ContextWrapper {

    private File file;
    private String repoDir;

    public JsonParserTask(Context context, File file) {
        super(context);
        this.file = file;
        this.repoDir = PathUtil.getRepoDirectory(context);
    }

    public Observable<RepoBundle> parse() {
        return Observable.create(emitter -> {
            boolean status = false;

            final Repo repo = RepoListManager.getRepoById(this, FilenameUtils.getBaseName(file.getName()));
            final File jsonFile = new File(repoDir + repo.getRepoId() + JSON);
            final Gson gson = new Gson();
            final AppDatabase appDatabase = AppDatabase.getAppDatabase(this);
            final AppDao appDao = appDatabase.appDao();
            final PackageDao packageDao = appDatabase.packageDao();
            try {
                final String jsonString = IOUtils.toString(FileUtils.openInputStream(jsonFile), "UTF-8");
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
                status = true;
            } catch (JSONException | IOException e) {
                e.printStackTrace();
            } catch (SQLiteConstraintException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            emitter.onNext(new RepoBundle(status, repo));
            emitter.onComplete();
        });

    }
}
