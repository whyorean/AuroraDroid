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

package com.aurora.adroid.fragment.preference;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.aurora.adroid.Constants;
import com.aurora.adroid.R;
import com.aurora.adroid.activity.SettingsActivity;
import com.aurora.adroid.util.PackageUtil;
import com.aurora.adroid.util.PathUtil;
import com.aurora.adroid.util.PrefUtil;
import com.aurora.adroid.util.Root;
import com.aurora.adroid.util.Util;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.scottyab.rootbeer.RootBeer;

public class InstallationFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String ROOT = "1";
    private static final String SERVICES = "2";

    private Context context;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        getPreferenceManager().setSharedPreferencesName(Constants.SHARED_PREFERENCES_KEY);
        setPreferencesFromResource(R.xml.preferences_installation, rootKey);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SharedPreferences mPrefs = Util.getPrefs(context);
        mPrefs.registerOnSharedPreferenceChangeListener(this);

        ListPreference listInstallMethod = findPreference(Constants.PREFERENCE_INSTALLATION_METHOD);
        assert listInstallMethod != null;
        listInstallMethod.setOnPreferenceChangeListener((preference, newValue) -> {
            String installMethod = (String) newValue;
            if (installMethod.equals(ROOT)) {
                RootBeer rootBeer = new RootBeer(context);
                if (rootBeer.isRooted()) {
                    PrefUtil.putString(context, Constants.PREFERENCE_INSTALLATION_METHOD, installMethod);
                    Root.requestRoot();
                    showDownloadDialog();
                    return true;
                } else {
                    showNoRootDialog();
                    return false;
                }
            } else if (installMethod.equals(SERVICES)) {
                if (PackageUtil.isInstalled(context, Constants.SERVICE_PACKAGE)) {
                    PrefUtil.putString(context, Constants.PREFERENCE_INSTALLATION_METHOD, installMethod);
                    PrefUtil.putString(context, Constants.PREFERENCE_DOWNLOAD_DIRECTORY, PathUtil.getExtBaseDirectory());
                    return true;
                } else {
                    showNoServicesDialog();
                    return false;
                }
            } else {
                PrefUtil.putString(context, Constants.PREFERENCE_INSTALLATION_METHOD, installMethod);
                return true;
            }
        });

        Preference servicePreference = findPreference(Constants.PREFERENCE_LAUNCH_SERVICES);
        assert servicePreference != null;
        if (PackageUtil.isInstalled(context, Constants.SERVICE_PACKAGE)) {
            servicePreference.setEnabled(true);
            servicePreference.setOnPreferenceClickListener(preference -> {
                Intent intent = context.getPackageManager().getLaunchIntentForPackage(Constants.SERVICE_PACKAGE);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                context.startActivity(intent);
                return false;
            });
        } else {
            servicePreference.setEnabled(false);
            servicePreference.setSummary(getString(R.string.pref_services_desc_alt));
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case Constants.PREFERENCE_INSTALLATION_METHOD:
                SettingsActivity.shouldRestart = true;
                break;
        }

    }

    private void showDownloadDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
        builder.setTitle(R.string.pref_downloads);
        builder.setMessage(R.string.pref_install_mode_root_warn);
        builder.setPositiveButton(android.R.string.ok, (dialog, which) -> dialog.dismiss());
        builder.create();
        builder.show();
    }

    private void showNoRootDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
        builder.setTitle(R.string.action_installations);
        builder.setMessage(R.string.pref_install_mode_no_root);
        builder.setPositiveButton(android.R.string.ok, (dialog, which) -> dialog.dismiss());
        builder.create();
        builder.show();
    }

    private void showNoServicesDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
        builder.setTitle(R.string.action_installations);
        builder.setMessage(R.string.pref_install_mode_no_services);
        builder.setPositiveButton(android.R.string.ok, (dialog, which) -> dialog.dismiss());
        builder.create();
        builder.show();
    }
}