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

package com.aurora.adroid.ui.setting;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;

import com.aurora.adroid.Constants;
import com.aurora.adroid.R;
import com.aurora.adroid.manager.LocaleManager;
import com.aurora.adroid.util.PrefUtil;
import com.aurora.adroid.util.TextUtil;
import com.aurora.adroid.util.Util;

import org.apache.commons.lang3.StringUtils;

import java.util.Locale;

public class LanguageFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    private Context context;
    private LocaleManager localeManager;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
        localeManager = new LocaleManager(context);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        getPreferenceManager().setSharedPreferencesName(Constants.SHARED_PREFERENCES_KEY);
        setPreferencesFromResource(R.xml.preferences_lang, rootKey);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SharedPreferences preferences = Util.getPrefs(context);
        preferences.registerOnSharedPreferenceChangeListener(this);

        ListPreference localeList = findPreference(Constants.PREFERENCE_LOCALE_LIST);
        assert localeList != null;
        localeList.setOnPreferenceChangeListener((preference, newValue) -> {
            String choice = newValue.toString();
            if (StringUtils.isEmpty(choice)) {
                PrefUtil.putBoolean(context, Constants.PREFERENCE_LOCALE_CUSTOM, false);
                localeManager.setNewLocale(Locale.getDefault(), false);
            } else {
                String lang = choice.split("-")[0];
                String country = choice.split("-")[1];
                Locale locale = new Locale(lang, country);
                localeManager.setNewLocale(locale, true);
            }
            return true;
        });
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case Constants.PREFERENCE_LOCALE_CUSTOM:
            case Constants.PREFERENCE_LOCALE_LIST: {
                SettingsActivity.shouldRestart = true;
                break;
            }
        }
    }
}
