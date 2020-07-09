
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

package com.aurora.adroid.model.v2;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import lombok.Data;

@Data
public class Localization {
    @SerializedName("featureGraphic")
    @Expose
    private String featureGraphic;
    @SerializedName("icon")
    @Expose
    private String icon;
    @SerializedName("phoneScreenshots")
    @Expose
    private List<String> phoneScreenshots;
    @SerializedName("summary")
    @Expose
    private String summary;
    @SerializedName("whatsNew")
    @Expose
    private String changelog;
    @SerializedName("description")
    @Expose
    private String description;
}
