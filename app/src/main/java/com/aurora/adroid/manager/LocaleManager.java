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
import android.content.res.Configuration;
import android.content.res.Resources;

import com.aurora.adroid.Constants;
import com.aurora.adroid.util.PrefUtil;
import com.aurora.adroid.util.Util;

import java.util.Locale;

public class LocaleManager {

    private Context context;

    public LocaleManager(Context context) {
        this.context = context;
    }

    public Locale getLocale() {
        return Util.isCustomLocaleEnabled(context) ? getCustomLocale() : Locale.getDefault();
    }

    private Locale getCustomLocale() {
        String language = PrefUtil.getString(context, Constants.PREFERENCE_LOCALE_LANG);
        String country = PrefUtil.getString(context, Constants.PREFERENCE_LOCALE_COUNTRY);
        if (language.equals("b")) {
            return new Locale(country);
        } else return new Locale(language, country);
    }

    public void setLocale() {
        updateResources(getLocale());
    }

    public void setNewLocale(Locale locale, boolean isCustom) {
        if (isCustom)
            saveLocale(locale);
        updateResources(locale);
    }

    private void saveLocale(Locale locale) {
        PrefUtil.putString(context, Constants.PREFERENCE_LOCALE_LANG, locale.getLanguage());
        PrefUtil.putString(context, Constants.PREFERENCE_LOCALE_COUNTRY, locale.getCountry());
        PrefUtil.putBoolean(context, Constants.PREFERENCE_LOCALE_CUSTOM, true);
    }

    private void updateResources(Locale locale) {
        Locale.setDefault(locale);
        Resources resources = context.getResources();
        Configuration configuration = new Configuration(resources.getConfiguration());
        configuration.locale = locale;
        resources.updateConfiguration(configuration, resources.getDisplayMetrics());
    }
}
