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

package com.aurora.adroid.task;

import android.content.Context;
import android.content.ContextWrapper;

import com.aurora.adroid.database.AppDao;
import com.aurora.adroid.database.AppDatabase;
import com.aurora.adroid.database.AppPackageDao;
import com.aurora.adroid.database.RepoDao;
import com.aurora.adroid.manager.RepoBundle;
import com.aurora.adroid.manager.RepoListManager;
import com.aurora.adroid.model.App;
import com.aurora.adroid.model.Package;
import com.aurora.adroid.model.StaticRepo;
import com.aurora.adroid.model.v2.AppPackage;
import com.aurora.adroid.model.v2.Index;
import com.aurora.adroid.util.PathUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.aurora.adroid.Constants.JSON;

public class JsonParserTask extends ContextWrapper {

    private File file;
    private String repoDir;
    private RepoListManager repoListManager;

    public JsonParserTask(Context context, File file) {
        super(context);
        this.file = file;
        this.repoDir = PathUtil.getRepoDirectory(context);
        this.repoListManager = new RepoListManager(context);
    }

    public RepoBundle parse() {

        boolean status = false;
        final AppDatabase appDatabase = AppDatabase.getDatabase(this);
        final AppDao appDao = appDatabase.appDao();
        final AppPackageDao packageDao = appDatabase.appPackageDao();
        final RepoDao repoDao = appDatabase.repoDao();

        final StaticRepo staticRepo = repoListManager.getRepoById(FilenameUtils.getBaseName(file.getName()));

        final File jsonFile = new File(repoDir + staticRepo.getRepoId() + JSON);
        final Gson gson = new GsonBuilder().excludeFieldsWithModifiers(Modifier.TRANSIENT).create();

        try {
            final String jsonString = IOUtils.toString(FileUtils.openInputStream(jsonFile), StandardCharsets.UTF_8);


            final Index index = gson.fromJson(jsonString, Index.class);
            final HashMap<String, List<Package>> packageHashMap = index.getPackages();

            final List<App> appList = new ArrayList<>();
            final List<AppPackage> appPackageList = new ArrayList<>();

            for (App app : index.getApps()) {
                app.setRepoId(staticRepo.getRepoId());
                app.setRepoName(staticRepo.getRepoName());
                app.setRepoUrl(staticRepo.getRepoUrl());
                List<Package> packageList = packageHashMap.get(app.getPackageName());

                //Create app package
                AppPackage appPackage = new AppPackage();
                appPackage.setRepoId(staticRepo.getRepoId());
                appPackage.setPackageName(app.getPackageName());
                appPackage.setPackageList(packageList);

                appList.add(app);
                appPackageList.add(appPackage);
            }

            appDatabase.getQueryExecutor().execute(() -> {
                appDao.insertAll(appList);
                packageDao.insertAll(appPackageList);
            });

            index.repo.setRepoId(staticRepo.getRepoId());
            repoDao.insert(index.repo);
            status = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new RepoBundle(status, staticRepo);
    }
}
