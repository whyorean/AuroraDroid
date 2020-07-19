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
import com.aurora.adroid.model.App;
import com.aurora.adroid.util.PrefUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class FavouritesManager {

    private Context context;
    private Gson gson;

    public FavouritesManager(Context context) {
        this.context = context;
        this.gson = new Gson();
    }

    public void addToFavourites(App app) {
        final List<App> stringList = getFavouriteApps();
        if (!stringList.contains(app)) {
            stringList.add(app);
            saveFavourites(stringList);
        }
    }

    public void addToFavourites(List<App> appList) {
        final List<App> favouriteApps = getFavouriteApps();
        for (App app : appList) {
            if (!favouriteApps.contains(app)) {
                favouriteApps.add(app);
            }
        }
        saveFavourites(favouriteApps);
    }

    public void removeFromFavourites(App app) {
        final List<App> favouriteApps = getFavouriteApps();
        if (favouriteApps.contains(app)) {
            favouriteApps.remove(app);
            saveFavourites(favouriteApps);
        }
    }

    public boolean isFavourite(App app) {
        return getFavouriteApps().contains(app);
    }

    public void clear() {
        saveFavourites(new ArrayList<>());
    }

    private void saveFavourites(List<App> appList) {
        PrefUtil.putString(context, Constants.PREFERENCE_FAVOURITE_APPS, gson.toJson(appList));
    }

    public List<App> getFavouriteApps() {
        String rawList = PrefUtil.getString(context, Constants.PREFERENCE_FAVOURITE_APPS);
        Type type = new TypeToken<List<App>>() {
        }.getType();
        List<App> stringList = gson.fromJson(rawList, type);

        if (stringList == null)
            return new ArrayList<>();
        else
            return stringList;
    }
}
