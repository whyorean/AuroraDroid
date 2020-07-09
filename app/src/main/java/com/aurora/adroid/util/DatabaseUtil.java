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

package com.aurora.adroid.util;

import android.content.Context;

import com.aurora.adroid.Constants;
import com.aurora.adroid.model.App;
import com.aurora.adroid.model.Package;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class DatabaseUtil {

    public static boolean isRepoAvailable(Context context) {
        return PrefUtil.getBoolean(context, Constants.REPO_AVAILABLE);
    }

    public static boolean isDatabaseAvailable(Context context) {
        return PrefUtil.getBoolean(context, Constants.DATABASE_AVAILABLE);
    }

    public static void setDatabaseAvailable(Context context, boolean available) {
        PrefUtil.putBoolean(context, Constants.DATABASE_AVAILABLE, available);
    }

    public static boolean isDatabaseObsolete(Context context) {
        try {
            long interval = Long.parseLong(PrefUtil.getString(context, Constants.PREFERENCE_REPO_UPDATE_INTERVAL));
            if (interval == 0)
                return false;

            long lastSyncDate = Long.parseLong(PrefUtil.getString(context, Constants.DATABASE_DATE));
            long currentSyncDate = Calendar.getInstance().getTimeInMillis();
            long diffDatesInMillis = currentSyncDate - lastSyncDate;
            long diffInDays = TimeUnit.MILLISECONDS.toDays(diffDatesInMillis);
            return diffInDays > interval;
        } catch (Exception e) {
            return false;
        }
    }

    public static void setDatabaseSyncTime(Context context, Long dateInMillis) {
        PrefUtil.putString(context, Constants.DATABASE_DATE, String.valueOf(dateInMillis));
    }

    public static String getImageUrl(App app) {
        return app.getRepoUrl() + Constants.IMG_URL_PREFIX + app.getIcon();
    }

    public static String getDownloadURl(App app) {
        return app.getRepoUrl() + "/" + app.getPkg().getApkName();
    }

    public static String getDownloadURl(App app, Package pkg) {
        return app.getRepoUrl() + "/" + pkg.getApkName();
    }
}
