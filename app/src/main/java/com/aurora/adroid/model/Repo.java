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

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.jetbrains.annotations.NotNull;

@Entity(tableName = "repo")
public class Repo {
    @PrimaryKey
    @SerializedName("repoId")
    @NotNull
    @Expose
    private String repoId="00";
    @SerializedName("repoName")
    @Expose
    private String repoName = "";
    @SerializedName("repoUrl")
    @Expose
    private String repoUrl = "";
    @SerializedName("repoMirrors")
    @Expose
    private String[] repoMirrors;
    @SerializedName("repoFingerprint")
    @Expose
    private String repoFingerprint = "NA";
    @SerializedName("repoDescription")
    @Expose
    private String repoDescription = "NA";

    public String[] getRepoMirrors() {
        return repoMirrors;
    }

    public void setRepoMirrors(String[] repoMirrors) {
        this.repoMirrors = repoMirrors;
    }

    public String getRepoId() {
        return repoId;
    }

    public void setRepoId(@NotNull String repoId) {
        this.repoId = repoId;
    }

    public String getRepoDescription() {
        return repoDescription;
    }

    public void setRepoDescription(String repoDescription) {
        this.repoDescription = repoDescription;
    }

    public String getRepoName() {
        return repoName;
    }

    public void setRepoName(String repoName) {
        this.repoName = repoName;
    }

    public String getRepoUrl() {
        return repoUrl;
    }

    public void setRepoUrl(String repoUrl) {
        this.repoUrl = repoUrl;
    }

    public String getRepoFingerprint() {
        return repoFingerprint;
    }

    public void setRepoFingerprint(String repoFingerprint) {
        this.repoFingerprint = repoFingerprint;
    }
}
