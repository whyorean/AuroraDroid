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

package com.aurora.adroid.adapter;

import android.content.Context;

import com.aurora.adroid.model.App;

import java.util.List;

public class UpdatableAppsAdapter extends InstalledAppsAdapter {

    public UpdatableAppsAdapter(Context context) {
        super(context);
    }

    @Override
    protected void getDetails(List<String> versionList, List<String> extraList, App app) {
        versionList.add(app.getAppPackage().getVersionName() + "." + app.getAppPackage().getVersionCode());
        extraList.add(app.isSystemApp() ? "System App" : "User App");
    }
}
