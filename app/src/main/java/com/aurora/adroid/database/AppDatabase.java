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

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.aurora.adroid.model.App;
import com.aurora.adroid.model.v2.AppPackage;
import com.aurora.adroid.model.v2.Repo;
import com.aurora.adroid.util.Log;

@Database(entities = {App.class, AppPackage.class, Repo.class}, version = 3, exportSchema = false)
@TypeConverters(DatabaseConverter.class)
public abstract class AppDatabase extends RoomDatabase {

    private static final String DATABASE_NAME = "AuroraDroid_2";
    private static AppDatabase instance;

    public static synchronized AppDatabase getDatabase(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, AppDatabase.DATABASE_NAME)
                            .fallbackToDestructiveMigration()
                            .addCallback(new Callback() {
                                @Override
                                public void onOpen(@NonNull SupportSQLiteDatabase db) {
                                    super.onOpen(db);
                                    Log.i("DB connection established");
                                }

                                @Override
                                public void onDestructiveMigration(@NonNull SupportSQLiteDatabase db) {
                                    super.onDestructiveMigration(db);
                                    Log.i("DB migrated to new version, old purged");
                                }
                            })
                            .build();
                }
            }
        }
        return instance;
    }

    public static void destroyInstance() {
        if (instance != null) {
            instance.close();
            instance = null;
        }
    }

    public boolean clearAll() {
        instance.clearAllTables();
        return true;
    }

    public abstract AppDao appDao();

    public abstract AppPackageDao appPackageDao();

    public abstract RepoDao repoDao();
}
