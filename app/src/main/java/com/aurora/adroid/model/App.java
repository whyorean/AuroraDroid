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

import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.Ignore;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity(tableName = "app", primaryKeys = {"repoId", "packageName"})
public class App {
    @SerializedName("repoId")
    @Expose
    @NotNull
    private String repoId = "01";
    @SerializedName("repoName")
    @Expose
    private String repoName;
    @SerializedName("added")
    @Expose
    private Long added;
    @SerializedName("authorName")
    @Expose
    private String authorName = "unknown";
    @SerializedName("authorEmail")
    @Expose
    private String authorEmail = "unknown";
    @SerializedName("bitcoin")
    @Expose
    private String bitcoin;
    @SerializedName("categories")
    @Expose
    private List<String> categories = new ArrayList<>();
    @SerializedName("description")
    @Expose
    private String description;
    @SerializedName("donate")
    @Expose
    private String donate;
    @SerializedName("icon")
    @Expose
    private String icon;
    @SerializedName("issueTracker")
    @Expose
    private String issueTracker;
    @SerializedName("lastUpdated")
    @Expose
    private Long lastUpdated;
    @SerializedName("license")
    @Expose
    private String license;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("packageName")
    @Expose
    @NotNull
    private String packageName = "unknown";
    @SerializedName("sourceCode")
    @Expose
    private String sourceCode;
    @SerializedName("suggestedVersionCode")
    @Expose
    private String suggestedVersionCode;
    @SerializedName("suggestedVersionName")
    @Expose
    private String suggestedVersionName;
    @SerializedName("summary")
    @Expose
    private String summary;
    @SerializedName("repoUrl")
    @Expose
    private String repoUrl = "fdroid";
    @SerializedName("webSite")
    @Expose
    private String webSite;
    @SerializedName("localized")
    @Expose
    @Embedded
    private Localized localized;
    @Ignore
    private boolean installed;
    @Ignore
    private boolean systemApp;
    @Ignore
    private String screenShots = null;
    @Ignore
    private Drawable iconDrawable;
    @Ignore
    private Set<String> permissions = new HashSet<>();
    @Ignore
    private Package appPackage = new Package();
    @Ignore
    private List<Package> packageList = new ArrayList<>();
    @Ignore
    private PackageInfo packageInfo;

    public App() {
        this.packageInfo = new PackageInfo();
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

    public List<Package> getPackageList() {
        return packageList;
    }

    public void setPackageList(List<Package> packageList) {
        this.setAppPackage(packageList.get(0));
        this.packageList = packageList;
    }

    public String getRepoName() {
        return repoName;
    }

    public void setRepoName(String repoName) {
        this.repoName = repoName;
    }

    @NotNull
    public String getRepoId() {
        return repoId;
    }

    public void setRepoId(@NotNull String repoId) {
        this.repoId = repoId;
    }

    public String getRepoUrl() {
        return repoUrl;
    }

    public void setRepoUrl(String repoUrl) {
        this.repoUrl = repoUrl;
    }

    public String getScreenShots() {
        return screenShots;
    }

    public void setScreenShots(String screenShots) {
        this.screenShots = screenShots;
    }

    public Localized getLocalized() {
        return localized;
    }

    public void setLocalized(Localized localized) {
        this.localized = localized;
    }

    public Package getAppPackage() {
        return appPackage;
    }

    public void setAppPackage(Package appPackage) {
        this.appPackage = appPackage;
    }

    public String getAuthorEmail() {
        return authorEmail;
    }

    public void setAuthorEmail(String authorEmail) {
        this.authorEmail = authorEmail;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public Drawable getIconDrawable() {
        return iconDrawable;
    }

    public void setIconDrawable(Drawable iconDrawable) {
        this.iconDrawable = iconDrawable;
    }

    public Set<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(Collection<String> permissions) {
        this.permissions = new HashSet<>(permissions);
    }

    public boolean isInstalled() {
        return installed;
    }

    public void setInstalled(boolean installed) {
        this.installed = installed;
    }

    public boolean isSystemApp() {
        return systemApp;
    }

    public void setSystemApp(boolean systemApp) {
        this.systemApp = systemApp;
    }

    public PackageInfo getPackageInfo() {
        return packageInfo;
    }

    public void setPackageInfo(PackageInfo packageInfo) {
        this.packageInfo = packageInfo;
        this.setInstalled(true);
        this.setSystemApp(null != packageInfo.applicationInfo && (packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0);
    }

    public Long getAdded() {
        return added;
    }

    public void setAdded(Long added) {
        this.added = added;
    }

    public String getBitcoin() {
        return bitcoin;
    }

    public void setBitcoin(String bitcoin) {
        this.bitcoin = bitcoin;
    }

    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDonate() {
        return donate;
    }

    public void setDonate(String donate) {
        this.donate = donate;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getIssueTracker() {
        return issueTracker;
    }

    public void setIssueTracker(String issueTracker) {
        this.issueTracker = issueTracker;
    }

    public Long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public String getLicense() {
        return license;
    }

    public void setLicense(String license) {
        this.license = license;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @NotNull
    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(@NotNull String packageName) {
        this.packageName = packageName;
    }

    public String getSourceCode() {
        return sourceCode;
    }

    public void setSourceCode(String sourceCode) {
        this.sourceCode = sourceCode;
    }

    public String getSuggestedVersionCode() {
        return suggestedVersionCode;
    }

    public void setSuggestedVersionCode(String suggestedVersionCode) {
        this.suggestedVersionCode = suggestedVersionCode;
    }

    public String getSuggestedVersionName() {
        return suggestedVersionName;
    }

    public void setSuggestedVersionName(String suggestedVersionName) {
        this.suggestedVersionName = suggestedVersionName;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getWebSite() {
        return webSite;
    }

    public void setWebSite(String webSite) {
        this.webSite = webSite;
    }

}

