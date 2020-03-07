package com.aurora.adroid.model;

import androidx.room.PrimaryKey;

import org.jetbrains.annotations.NotNull;

import lombok.Data;

@Data
public class RepoHeader implements Comparable<RepoHeader> {

    @PrimaryKey
    @NotNull
    private String repoId = "00";
    private Long lastModified;

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof RepoHeader))
            return false;
        return repoId.equalsIgnoreCase(((RepoHeader) obj).getRepoId());
    }

    @Override
    public int hashCode() {
        return repoId.hashCode();
    }

    @Override
    public int compareTo(@NotNull RepoHeader repoHeader) {
        return getRepoId().compareToIgnoreCase(repoHeader.getRepoId());
    }
}
