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

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.apache.commons.lang3.ObjectUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Entity(tableName = "package")
public class Package {

    @SerializedName("repoName")
    @Expose
    private String repoName;
    @SerializedName("repoUrl")
    @Expose
    private String repoUrl;
    @SerializedName("added")
    @Expose
    private Long added;
    @SerializedName("apkName")
    @Expose
    @PrimaryKey
    @NotNull
    private String apkName = "unknown";
    @SerializedName("hash")
    @Expose
    private String hash;
    @SerializedName("hashType")
    @Expose
    private String hashType;
    @SerializedName("minSdkVersion")
    @Expose
    private String minSdkVersion;
    @SerializedName("packageName")
    @Expose
    @NonNull
    private String packageName = "c";
    @SerializedName("sig")
    @Expose
    private String sig;
    @SerializedName("signer")
    @Expose
    private String signer;
    @SerializedName("size")
    @Expose
    private Long size;
    @SerializedName("srcname")
    @Expose
    private String srcname;
    @SerializedName("targetSdkVersion")
    @Expose
    private String targetSdkVersion;
    @SerializedName("uses-permission")
    @Expose
    private List<List<String>> usesPermission = null;
    @SerializedName("versionCode")
    @Expose
    private Long versionCode=0L;
    @SerializedName("versionName")
    @Expose
    private String versionName="null";

    public Package() {
    }

    public String getRepoUrl() {
        return repoUrl;
    }

    public void setRepoUrl(String repoUrl) {
        this.repoUrl = repoUrl;
    }

    public String getRepoName() {
        return repoName;
    }

    public void setRepoName(String repoName) {
        this.repoName = repoName;
    }

    public Long getAdded() {
        return added;
    }

    public void setAdded(Long added) {
        this.added = added;
    }

    @NotNull
    public String getApkName() {
        return apkName;
    }

    public void setApkName(@NotNull String apkName) {
        this.apkName = apkName;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getHashType() {
        return hashType;
    }

    public void setHashType(String hashType) {
        this.hashType = hashType;
    }

    public String getMinSdkVersion() {
        return minSdkVersion;
    }

    public void setMinSdkVersion(String minSdkVersion) {
        this.minSdkVersion = minSdkVersion;
    }

    @NotNull
    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(@NotNull String packageName) {
        this.packageName = packageName;
    }

    public String getSig() {
        return sig;
    }

    public void setSig(String sig) {
        this.sig = sig;
    }

    public String getSigner() {
        return signer;
    }

    public void setSigner(String signer) {
        this.signer = signer;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public String getSrcname() {
        return srcname;
    }

    public void setSrcname(String srcname) {
        this.srcname = srcname;
    }

    public String getTargetSdkVersion() {
        return targetSdkVersion;
    }

    public void setTargetSdkVersion(String targetSdkVersion) {
        this.targetSdkVersion = targetSdkVersion;
    }

    public List<List<String>> getUsesPermission() {
        return usesPermission;
    }

    public void setUsesPermission(List<List<String>> usesPermission) {
        this.usesPermission = usesPermission;
    }

    public Long getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(Long versionCode) {
        this.versionCode = versionCode;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }
}
