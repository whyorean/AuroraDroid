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

package com.aurora.adroid.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.aurora.adroid.database.AppRepository;
import com.aurora.adroid.model.App;

import java.util.List;

public class ClusterAppsViewModel extends AndroidViewModel {

    private AppRepository appRepository;

    public ClusterAppsViewModel(@NonNull Application application) {
        super(application);
        appRepository = new AppRepository(application);
    }

    public LiveData<List<App>> getNewAppsLiveData() {
        return appRepository.getAllNewApps(System.currentTimeMillis(), 14);
    }

    public LiveData<List<App>> getUpdatedAppsLiveData() {
        return appRepository.getAllUpdatedApps(System.currentTimeMillis(), 14);
    }

    public LiveData<List<App>> getCategoryAppsLiveData(String category) {
        return appRepository.getAllAppsByCategory(category);
    }

    public LiveData<List<App>> getRepoAppsLiveData(String repoId) {
        return appRepository.getAllAppsByRepositoryId(repoId);
    }

    public LiveData<List<App>> getAuthorAppsLiveData(String authorName) {
        return appRepository.getAllAppsByDeveloper(authorName);
    }
}
