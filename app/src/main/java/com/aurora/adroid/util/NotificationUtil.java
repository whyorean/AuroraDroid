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

package com.aurora.adroid.util;

import android.content.Context;

import com.aurora.adroid.Constants;

import java.util.Map;

public class NotificationUtil {

    private static final String PSEUDO_NOTIFICATION_MAP = "PSEUDO_NOTIFICATION_MAP";

    public static boolean isNotificationEnabled(Context context) {
        return Util.getPrefs(context).getBoolean(Constants.PREFERENCE_NOTIFICATION_TOGGLE, true);
    }

    public static Boolean shouldNotify(Context context, String packageName) {
        Map<String, String> pseudoMap = getDNDNotificationMap(context);
        String value = Util.emptyIfNull(pseudoMap.get(packageName));
        return !value.equals("DND");
    }

    private static Map<String, String> getDNDNotificationMap(Context context) {
        return PrefUtil.getMap(context, PSEUDO_NOTIFICATION_MAP);
    }

    public static void updateDNDNotificationMap(Context context, String packageName, String value) {
        Map<String, String> pseudoMap = getDNDNotificationMap(context);
        pseudoMap.put(packageName, value);
        PrefUtil.saveMap(context, pseudoMap, PSEUDO_NOTIFICATION_MAP);
    }
}
