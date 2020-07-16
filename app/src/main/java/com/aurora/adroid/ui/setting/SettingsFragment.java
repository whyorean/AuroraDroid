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

import android.os.Bundle;

import androidx.annotation.IdRes;
import androidx.annotation.Nullable;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.aurora.adroid.R;

public class SettingsFragment extends PreferenceFragmentCompat {

    private NavController navController;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        navController = NavHostFragment.findNavController(this);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences_main, rootKey);

        Preference preferenceDownload = getPreferenceScreen().findPreference("PREFERENCE_DOWNLOAD_ENTRY");
        if (preferenceDownload != null)
            preferenceDownload.setOnPreferenceClickListener(p -> {
                navigate(R.id.action_preferenceSetting_to_preferenceDownload);
                return false;
            });

        Preference preferenceInstallation = getPreferenceScreen().findPreference("PREFERENCE_INSTALLATION_ENTRY");
        if (preferenceInstallation != null)
            preferenceInstallation.setOnPreferenceClickListener(p -> {
                navigate(R.id.action_preferenceSetting_to_preferenceInstallation);
                return false;
            });

        Preference preferenceLanguage = getPreferenceScreen().findPreference("PREFERENCE_LANGUAGE_ENTRY");
        if (preferenceLanguage != null)
            preferenceLanguage.setOnPreferenceClickListener(p -> {
                navigate(R.id.action_preferenceSetting_to_preferenceLanguage);
                return false;
            });

        Preference preferenceNetwork = getPreferenceScreen().findPreference("PREFERENCE_NETWORK_ENTRY");
        if (preferenceNetwork != null)
            preferenceNetwork.setOnPreferenceClickListener(p -> {
                navigate(R.id.action_preferenceSetting_to_preferenceNetworks);
                return false;
            });

        Preference preferenceUI = getPreferenceScreen().findPreference("PREFERENCE_UI_ENTRY");
        if (preferenceUI != null)
            preferenceUI.setOnPreferenceClickListener(p -> {
                navigate(R.id.action_preferenceSetting_to_preferenceUI);
                return false;
            });

        Preference preferenceUpdate = getPreferenceScreen().findPreference("PREFERENCE_UPDATE_ENTRY");
        if (preferenceUpdate != null)
            preferenceUpdate.setOnPreferenceClickListener(p -> {
                navigate(R.id.action_preferenceSetting_to_preferenceUpdates);
                return false;
            });
    }

    private void navigate(@IdRes int actionId) {
        if (navController != null) {
            navController.navigate(actionId);
        }
    }
}