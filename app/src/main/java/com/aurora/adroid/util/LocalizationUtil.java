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

import com.aurora.adroid.R;
import com.aurora.adroid.model.App;
import com.aurora.adroid.model.v2.Localization;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public abstract class LocalizationUtil {

    private static Localization getDefaultLocalization(HashMap<String, Localization> localizationHashMap) {
        Locale locale = Locale.getDefault();
        return localizationHashMap.get(locale.getLanguage());
    }

    private static Localization getEnforcedLocalization(HashMap<String, Localization> localizationHashMap) {
        Localization localization = localizationHashMap.get("en-US");
        if (localization == null)
            localization = localizationHashMap.get("en");
        return localization;
    }

    public static String getLocalizedSummary(Context context, App app) {

        HashMap<String, Localization> localizationHashMap = app.getLocalizationMap();

        if (localizationHashMap != null) {
            Localization preferredLocalization = getDefaultLocalization(localizationHashMap);
            if (preferredLocalization != null && preferredLocalization.getSummary() != null) {
                return preferredLocalization.getSummary();
            }

            Localization enforcedLocalization = getEnforcedLocalization(localizationHashMap);
            if (enforcedLocalization != null && enforcedLocalization.getSummary() != null) {
                return enforcedLocalization.getSummary();
            }
        }

        if (app.getSummary() != null)
            return app.getSummary();

        return context.getString(R.string.details_no_description);
    }

    public static String getLocalizedDescription(Context context, App app) {

        HashMap<String, Localization> localizationHashMap = app.getLocalizationMap();

        if (localizationHashMap != null) {
            Localization preferredLocalization = getDefaultLocalization(localizationHashMap);
            if (preferredLocalization != null && preferredLocalization.getDescription() != null) {
                return preferredLocalization.getDescription();
            }

            Localization enforcedLocalization = getEnforcedLocalization(localizationHashMap);
            if (enforcedLocalization != null && enforcedLocalization.getDescription() != null) {
                return enforcedLocalization.getDescription();
            }
        }

        if (app.getDescription() != null)
            return app.getDescription();

        return context.getString(R.string.details_no_description);
    }

    public static String getLocalizedChangelog(Context context, App app) {

        Locale locale = Locale.getDefault();
        HashMap<String, Localization> localizationHashMap = app.getLocalizationMap();

        if (localizationHashMap != null) {
            Localization preferredLocalization = getDefaultLocalization(localizationHashMap);
            if (preferredLocalization != null && preferredLocalization.getChangelog() != null) {
                return preferredLocalization.getChangelog();
            }

            Localization enforcedLocalization = getEnforcedLocalization(localizationHashMap);
            if (enforcedLocalization != null && enforcedLocalization.getChangelog() != null) {
                return enforcedLocalization.getChangelog();
            }
        }

        return context.getString(R.string.details_no_changes);
    }

    public static List<String> getScreenShots(App app) {
        final Locale locale = Locale.getDefault();
        final HashMap<String, Localization> localizationHashMap = app.getLocalizationMap();
        final List<String> screenshotUrls = new ArrayList<>();

        if (localizationHashMap != null) {
            String countryCode = locale.getLanguage();
            Localization enforcedLocalization = localizationHashMap.get(countryCode);

            if (enforcedLocalization == null || enforcedLocalization.getPhoneScreenshots() == null) {
                countryCode = "en-US";
                enforcedLocalization = localizationHashMap.get(countryCode);
            }

            if (enforcedLocalization == null || enforcedLocalization.getPhoneScreenshots() == null) {
                countryCode = "en";
                enforcedLocalization = localizationHashMap.get(countryCode);
            }

            /*
             * This hierarchy can further be improved
             * TODO: Select Screenshots based on device type & size
             */

            if (enforcedLocalization != null && enforcedLocalization.getPhoneScreenshots() != null) {
                final List<String> screenshotFiles = enforcedLocalization.getPhoneScreenshots();
                if (screenshotFiles != null) {
                    screenshotUrls.addAll(listToURLList(screenshotFiles, countryCode, "phoneScreenshots", app));
                }
            }

            if (enforcedLocalization != null && screenshotUrls.isEmpty() && enforcedLocalization.getSevenInchScreenshots() != null) {
                final List<String> screenshotFiles = enforcedLocalization.getSevenInchScreenshots();
                if (screenshotFiles != null) {
                    screenshotUrls.addAll(listToURLList(screenshotFiles, countryCode, "sevenInchScreenshots", app));
                }
            }

            if (enforcedLocalization != null && screenshotUrls.isEmpty() && enforcedLocalization.getTenInchScreenshots() != null) {
                final List<String> screenshotFiles = enforcedLocalization.getSevenInchScreenshots();
                if (screenshotFiles != null) {
                    screenshotUrls.addAll(listToURLList(screenshotFiles, countryCode, "tenInchScreenshots", app));
                }
            }

            if (enforcedLocalization != null && screenshotUrls.isEmpty() && enforcedLocalization.getWearScreenshots() != null) {
                final List<String> screenshotFiles = enforcedLocalization.getSevenInchScreenshots();
                if (screenshotFiles != null) {
                    screenshotUrls.addAll(listToURLList(screenshotFiles, countryCode, "wearScreenshots", app));
                }
            }
        }
        return screenshotUrls;
    }

    private static List<String> listToURLList(List<String> stringList, String locale, String prefix, App app) {
        final List<String> urlList = new ArrayList<>();
        for (String fileName : stringList)
            urlList.add(StringUtils.joinWith("/", app.getRepoUrl(),
                    app.getPackageName(),
                    locale,
                    prefix,
                    fileName));
        return urlList;
    }
}
