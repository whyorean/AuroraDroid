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

package com.aurora.adroid.manager;

import android.content.Context;

import com.aurora.adroid.Constants;
import com.aurora.adroid.util.PrefUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

public class BlacklistManager {

    private Context context;
    private Gson gson;

    public BlacklistManager(Context context) {
        this.context = context;
        this.gson = new Gson();
    }

    public void addToBlacklist(Set<String> packageNames) {
        Set<String> stringList = getBlacklistedPackages();
        stringList.addAll(packageNames);
        saveBlacklist(stringList);
    }

    public void addToBlacklist(String packageName) {
        Set<String> stringList = getBlacklistedPackages();
        stringList.add(packageName);
        saveBlacklist(stringList);
    }

    public void removeFromBlacklist(String packageName) {
        Set<String> stringList = getBlacklistedPackages();
        stringList.remove(packageName);
        saveBlacklist(stringList);
    }

    public boolean isBlacklisted(String packageName) {
        return getBlacklistedPackages().contains(packageName);
    }

    public void clear() {
        saveBlacklist(new HashSet<>());
    }

    private void saveBlacklist(Set<String> stringList) {
        PrefUtil.putString(context, Constants.PREFERENCE_BLACKLIST_PACKAGE_LIST, gson.toJson(stringList));
    }

    public Set<String> getBlacklistedPackages() {
        String rawList = PrefUtil.getString(context, Constants.PREFERENCE_BLACKLIST_PACKAGE_LIST);
        Type type = new TypeToken<Set<String>>() {
        }.getType();
        Set<String> stringList = gson.fromJson(rawList, type);

        if (stringList == null)
            return new HashSet<>();
        else
            return stringList;
    }
}
