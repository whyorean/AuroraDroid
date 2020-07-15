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

package com.aurora.adroid.model;

import android.content.pm.PackageInfo;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.TypeConverters;

import com.aurora.adroid.database.DatabaseConverter;
import com.aurora.adroid.model.v2.AppPackage;
import com.aurora.adroid.model.v2.Localization;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import lombok.Data;

@Data

@Entity(tableName = "app", primaryKeys = {"repoId", "packageName"})
@TypeConverters(DatabaseConverter.class)
public class App {
    @NotNull
    private String repoId = StringUtils.EMPTY;
    @NonNull
    private String packageName = StringUtils.EMPTY;

    private Long added;
    private String authorName = "unknown";
    private String authorEmail = "unknown";
    private String bitcoin;
    private List<String> categories = new ArrayList<>();
    private String description;
    private String donate;
    private String icon;
    private String issueTracker;
    private Long lastUpdated;
    private String license;
    private String name;
    private String sourceCode;
    private long suggestedVersionCode;
    private String suggestedVersionName;
    private String summary;
    private String repoName;
    private String repoUrl = "https://f-droid.org/repo";
    private String webSite;
    private Package pkg;

    @SerializedName("localized")
    @Expose
    private HashMap<String, Localization> localizationMap;
    private List<String> antiFeatures;

    @Ignore
    private transient boolean installed;
    @Ignore
    private transient boolean systemApp;
    @Ignore
    private transient Drawable iconDrawable;
    @Ignore
    private transient PackageInfo packageInfo;
    @Ignore
    private transient AppPackage appPackage;

    public App() {
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof App))
            return false;
        return packageName.equals(((App) obj).getPackageName());
    }

    @Override
    public int hashCode() {
        return (packageName.isEmpty()) ? 0 : packageName.hashCode();
    }
}

