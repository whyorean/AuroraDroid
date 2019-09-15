package com.aurora.adroid.model;

import androidx.room.PrimaryKey;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.jetbrains.annotations.NotNull;

public class RepoHeader implements Comparable<RepoHeader> {

    @PrimaryKey
    @SerializedName("repoId")
    @NotNull
    @Expose
    private String repoId = "00";
    private Long lastModified;

    @NotNull
    public String getRepoId() {
        return repoId;
    }

    public void setRepoId(@NotNull String repoId) {
        this.repoId = repoId;
    }

    public Long getLastModified() {
        return lastModified;
    }

    public void setLastModified(Long lastModified) {
        this.lastModified = lastModified;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof RepoHeader))
            return false;
        return repoId.equalsIgnoreCase(((RepoHeader) obj).getRepoId());
    }

    @Override
    public int compareTo(@NotNull RepoHeader repoHeader) {
        return getRepoId().compareToIgnoreCase(repoHeader.getRepoId());
    }
}
