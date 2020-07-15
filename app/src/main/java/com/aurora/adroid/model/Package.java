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

import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class Package {
    @PrimaryKey
    @NotNull
    private String apkName = StringUtils.EMPTY;
    private Long added;
    private String hash;
    private String hashType;
    private String minSdkVersion;
    private List<String> nativecode;
    private String packageName;
    private String sig;
    private String signer;
    private Long size;
    private String srcname;
    private String targetSdkVersion;
    @SerializedName("uses-permission")
    private List<List<String>> usesPermission;
    private Long versionCode;
    private String versionName;
    private List<String> antiFeatures;

    @Ignore
    private transient boolean isCompatible;
    @Ignore
    private transient List<String> incompatibleReasons;
}
