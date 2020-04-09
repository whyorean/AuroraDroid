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

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import lombok.Data;

@Data
@Entity(tableName = "repo")
public class Repo implements Comparable<Repo> {
    @PrimaryKey
    private String repoId = "00";
    private String repoName = "";
    private String repoUrl = "";
    private String[] repoMirrors;
    private String repoFingerprint = "NA";
    private String repoDescription = "NA";

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Repo))
            return false;
        return repoId.equalsIgnoreCase(((Repo) obj).getRepoId());
    }

    @Override
    public int hashCode() {
        return repoId.hashCode();
    }

    @Override
    public int compareTo(Repo repo) {
        return getRepoId().compareToIgnoreCase(repo.getRepoId());
    }
}
