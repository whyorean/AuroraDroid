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

package com.aurora.adroid.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.aurora.adroid.model.Package;

import java.util.List;

@Dao
public interface PackageDao {

    @Query("SELECT * FROM Package WHERE packageName = :packageName ORDER BY added DESC")
    Package getPackageByPackageName(String packageName);

    @Query("SELECT * FROM Package WHERE packageName = :packageName ORDER BY added DESC")
    LiveData<Package> getLivePackageByPackageName(String packageName);

    @Query("SELECT * FROM Package WHERE packageName = :packageName ORDER BY added DESC")
    LiveData<List<Package>> getLivePackageListByPackageName(String packageName);

    @Query("SELECT * FROM Package WHERE packageName = :packageName ORDER BY added DESC")
    List<Package> getPackageListByPackageName(String packageName);

    @Query("SELECT * FROM Package WHERE packageName = :packageName and repoName =:repoName ORDER BY added DESC")
    List<Package> getPackageListByPackageNameAndRepo(String packageName, String repoName);

    @Query("DELETE FROM PACKAGE WHERE repoName =:repoName")
    void clearRepo(String repoName);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Package> packageList);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Package appPackage);

    @Delete
    void delete(Package user);

    @Update
    void update(Package appPackage);
}
