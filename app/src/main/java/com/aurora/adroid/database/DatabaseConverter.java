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

import androidx.room.TypeConverter;

import com.aurora.adroid.model.Package;
import com.aurora.adroid.model.v2.AppPackage;
import com.aurora.adroid.model.v2.Localization;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.HashMap;
import java.util.List;

public class DatabaseConverter {

    @TypeConverter
    public static List<String> restoreList(String listOfString) {
        return new Gson().fromJson(listOfString, new TypeToken<List<String>>() {
        }.getType());
    }

    @TypeConverter
    public static List<List<String>> restoreNestedList(String listOfString) {
        return new Gson().fromJson(listOfString, new TypeToken<List<List<String>>>() {
        }.getType());
    }

    @TypeConverter
    public static String saveList(List<String> listOfString) {
        return new Gson().toJson(listOfString);
    }

    @TypeConverter
    public static String saveNestedList(List<List<String>> listOfString) {
        return new Gson().toJson(listOfString);
    }

    @TypeConverter
    public static List<Package> stringToPackageList(String listOfString) {
        return new Gson().fromJson(listOfString, new TypeToken<List<Package>>() {
        }.getType());
    }

    @TypeConverter
    public static String packageListToString(List<Package> packageList) {
        return new Gson().toJson(packageList);
    }

    @TypeConverter
    public static Package stringToPackage(String listOfString) {
        return new Gson().fromJson(listOfString, Package.class);
    }

    @TypeConverter
    public static String packageToString(Package appPackage) {
        return new Gson().toJson(appPackage);
    }

    @TypeConverter
    public static AppPackage stringToAppPackage(String listOfString) {
        return new Gson().fromJson(listOfString, AppPackage.class);
    }

    @TypeConverter
    public static String appPackageToString(AppPackage appPackage) {
        return new Gson().toJson(appPackage);
    }

    @TypeConverter
    public static HashMap<String, Localization> stringToLocalizationHashMap(String listOfString) {
        return new Gson().fromJson(listOfString, new TypeToken<HashMap<String, Localization>>() {
        }.getType());
    }

    @TypeConverter
    public static String localizationMapToString(HashMap<String, Localization> localizationHashMap) {
        return new Gson().toJson(localizationHashMap);
    }
}
