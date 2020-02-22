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

package com.aurora.adroid.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.RawQuery;
import androidx.room.Update;
import androidx.sqlite.db.SupportSQLiteQuery;

import com.aurora.adroid.model.App;

import java.util.List;

@Dao
public interface AppDao {
    @Query("SELECT * FROM app")
    LiveData<List<App>> getAllApps();

    @Query("SELECT DISTINCT * FROM app WHERE packageName IN (:packageName)")
    LiveData<List<App>> getAppsByPackageName(List<String> packageName);

    @Query("SELECT * FROM app WHERE packageName = :packageName")
    LiveData<App> getLiveAppByPackageName(String packageName);

    @Query("SELECT * FROM app WHERE packageName = :packageName")
    App getAppByPackageName(String packageName);

    @Query("SELECT * FROM app WHERE name = :appName")
    LiveData<App> getAppByName(String appName);

    @Query("SELECT * FROM app WHERE name LIKE :pattern LIMIT 20")
    LiveData<List<App>> findAppsByName(String pattern);

    @Query("SELECT * FROM app WHERE (authorName = :authorName) or (authorName LIKE :authorName) LIMIT 20")
    LiveData<List<App>> getAppsByAuthorName(String authorName);

    @Query("SELECT * FROM app WHERE :refTime - lastUpdated <= :weekCount * 604800000 ORDER BY lastUpdated DESC")
    LiveData<List<App>> getLatestUpdatedApps(Long refTime, int weekCount);

    @Query("SELECT * FROM app WHERE :refTime - added <= :weekCount * 604800000 ORDER BY added DESC")
    LiveData<List<App>> getLatestAddedApps(Long refTime, int weekCount);

    @Query("SELECT * FROM app WHERE (name LIKE :query) OR (summary LIKE :query) LIMIT 30")
    LiveData<List<App>> searchApps(String query);

    @RawQuery()
    List<App> searchApps(SupportSQLiteQuery query);

    @Query("SELECT * FROM app WHERE categories LIKE :category")
    LiveData<List<App>> searchAppsByCategory(String category);

    @Query("SELECT * FROM app WHERE repoId LIKE :repoId")
    LiveData<List<App>> searchAppsByRepository(String repoId);

    @Query("SELECT `en-US-phoneScreenshots` FROM app WHERE packageName =:packageName")
    LiveData<String> getLivePhoneScreenshots(String packageName);

    @Query("SELECT `en-US-phoneScreenshots` FROM app WHERE packageName =:packageName")
    String getPhoneScreenshots(String packageName);

    @Query("DELETE FROM app WHERE repoId =:repoID")
    void clearRepo(String repoID);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<App> appList);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(App app);

    @Delete
    void delete(App user);

    @Update
    void update(App app);
}
