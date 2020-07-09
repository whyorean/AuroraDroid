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

import com.aurora.adroid.model.v2.Repo;

import java.util.List;

@Dao
public interface RepoDao {

    @Query("SELECT * FROM `repo`")
    LiveData<List<Repo>> getAllIndexes();

    @Query("SELECT * FROM `repo` WHERE repoId = :repoId LIMIT 1")
    Repo getRepoByRepoId(String repoId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Repo> indexList);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Repo repo);

    @Delete
    void delete(Repo repo);

    @Update
    void update(Repo repo);
}
