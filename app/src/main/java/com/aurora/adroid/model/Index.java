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

import org.jetbrains.annotations.NotNull;

import lombok.Data;

@Data
@Entity(tableName = "index")
public class Index implements Comparable<Index> {
    @PrimaryKey
    @NonNull
    private String repoId = "00";
    private Long timestamp;
    private int version;
    private String name;
    private String icon;
    private String address;
    private String description;

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Index))
            return false;
        return repoId.equalsIgnoreCase(((Index) obj).getRepoId());
    }

    @Override
    public int compareTo(@NotNull Index repo) {
        return getRepoId().compareToIgnoreCase(repo.getRepoId());
    }
}
