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

package com.aurora.adroid.model;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.Ignore;

import com.aurora.adroid.util.PackageUtil;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity(tableName = "app", primaryKeys = {"repoId", "packageName"})
public class App {
    @NotNull
    private String repoId = "01";
    private String repoName;
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
    @NonNull
    private String packageName = "unknown";
    private String sourceCode;
    private String suggestedVersionCode;
    private String suggestedVersionName;
    private String summary;
    private String repoUrl = "https://f-droid.org/repo";
    private String webSite;

    @Embedded
    private Localized localized;
    @Ignore
    private transient boolean installed;
    @Ignore
    private transient boolean systemApp;
    @Ignore
    private transient String screenShots = null;
    @Ignore
    private transient Drawable iconDrawable;
    @Ignore
    private transient Set<String> permissions = new HashSet<>();
    @Ignore
    private transient Package appPackage = new Package();
    @Ignore
    private transient List<Package> packageList = new ArrayList<>();
    @Ignore
    private transient PackageInfo packageInfo;

    public App() {
    }

    @Ignore
    public App(PackageInfo packageInfo) {
        this.setPackageInfo(packageInfo);
        this.setSuggestedVersionName(packageInfo.versionName);
        this.setSuggestedVersionCode(String.valueOf(packageInfo.versionCode));
        if (packageInfo.requestedPermissions != null) {
            this.setPermissions(Arrays.asList(packageInfo.requestedPermissions));
        }
    }

    public void setPackageList(List<Package> packageList) {
        Collections.sort(packageList, (package1, package2) -> package2.getAdded().compareTo(package1.getAdded()));
        this.setAppPackage(PackageUtil.getOptimumPackage(packageList));
        this.packageList = packageList;
    }

    public void setPermissions(Collection<String> permissions) {
        this.permissions = new HashSet<>(permissions);
    }

    public void setPackageInfo(PackageInfo packageInfo) {
        this.packageInfo = packageInfo;
        this.setInstalled(true);
        this.setSystemApp(null != packageInfo.applicationInfo && (packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0);
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

