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

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.aurora.adroid.Constants;
import com.aurora.adroid.R;
import com.aurora.adroid.util.PackageUtil;
import com.aurora.adroid.util.PrefUtil;
import com.aurora.adroid.util.Util;
import com.aurora.adroid.util.ViewUtil;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.topjohnwu.superuser.Shell;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class InstallationFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String ROOT = "1";
    private static final String SERVICES = "2";

    private CompositeDisposable disposable = new CompositeDisposable();
    private SharedPreferences sharedPreferences;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences_installation, rootKey);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sharedPreferences = Util.getPrefs(requireContext());
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);

        final ListPreference listInstallMethod = findPreference(Constants.PREFERENCE_INSTALLATION_METHOD);
        if (listInstallMethod != null) {
            listInstallMethod.setOnPreferenceChangeListener((preference, newValue) -> {
                String installMethod = (String) newValue;
                if (installMethod.equals(ROOT)) {
                    if (Shell.getShell().isRoot()) {
                        PrefUtil.putString(requireContext(), Constants.PREFERENCE_INSTALLATION_METHOD, installMethod);
                        //showDownloadDialog();
                        return true;
                    } else {
                        showNoRootDialog();
                        return false;
                    }
                } else if (installMethod.equals(SERVICES)) {
                    if (PackageUtil.isInstalled(requireContext(), Constants.SERVICE_PACKAGE)) {
                        PrefUtil.putString(requireContext(), Constants.PREFERENCE_INSTALLATION_METHOD, installMethod);
                        //PrefUtil.putString(requireContext(), Constants.PREFERENCE_DOWNLOAD_DIRECTORY, PathUtil.getExtBaseDirectory());
                        return true;
                    } else {
                        showNoServicesDialog();
                        return false;
                    }
                } else {
                    PrefUtil.putString(requireContext(), Constants.PREFERENCE_INSTALLATION_METHOD, installMethod);
                    return true;
                }
            });
        }

        final Preference servicePreference = findPreference(Constants.PREFERENCE_LAUNCH_SERVICES);
        if (servicePreference != null) {
            if (PackageUtil.isInstalled(requireContext(), Constants.SERVICE_PACKAGE)) {
                servicePreference.setEnabled(true);
                servicePreference.setOnPreferenceClickListener(preference -> {
                    Intent intent = requireContext().getPackageManager().getLaunchIntentForPackage(Constants.SERVICE_PACKAGE);
                    intent.addCategory(Intent.CATEGORY_LAUNCHER);
                    requireContext().startActivity(intent);
                    return false;
                });
            } else {
                servicePreference.setSummary(getString(R.string.pref_services_desc_alt));
                servicePreference.setOnPreferenceClickListener(preference -> {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://gitlab.com/AuroraOSS/AuroraServices"));
                    startActivity(intent);
                    return false;
                });
            }
        }

        final ListPreference userProfilePreference = findPreference(Constants.PREFERENCE_INSTALLATION_PROFILE);
        if (userProfilePreference != null) {
            addUserInfoData(userProfilePreference);
        }
    }

    private List<String> getUserInfo() {
        List<String> rawUserList = new ArrayList<>();
        if (Shell.getShell().isRoot()) {
            Shell.Result result = Shell.su("pm list users").exec();
            if (result.isSuccess()) {
                List<String> profileEntries = result.getOut();
                for (String profile : profileEntries) {
                    Pattern p = Pattern.compile("\\{(.*):");
                    Matcher m = p.matcher(profile);
                    while (m.find()) {
                        String rawUser = m.group(1);
                        rawUserList.add(rawUser);
                    }
                }
            }
        }
        return rawUserList;
    }

    private void addUserInfoData(ListPreference listPreference) {
        disposable.add(Observable.fromCallable(this::getUserInfo)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(rawUserProfiles -> {
                    if (!rawUserProfiles.isEmpty()) {

                        listPreference.setEntries(new CharSequence[0]);
                        listPreference.setEntryValues(new CharSequence[0]);

                        List<String> entryList = new ArrayList<>();
                        List<String> entryValueList = new ArrayList<>();

                        for (String rawUser : rawUserProfiles) {
                            String[] rawUserArray = rawUser.split(":");
                            entryValueList.add(rawUserArray[0]);
                            entryList.add(rawUserArray[1]);
                        }

                        CharSequence[] entries = new CharSequence[entryList.size()];
                        CharSequence[] entryValues = new CharSequence[entryValueList.size()];
                        for (int i = 0; i < entryList.size(); i++) {
                            entries[i] = entryList.get(i);
                            entryValues[i] = entryValueList.get(i);
                        }

                        listPreference.setEntries(entries);
                        listPreference.setEntryValues(entryValues);
                    } else {
                        listPreference.setEnabled(false);
                    }
                }));
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case Constants.PREFERENCE_INSTALLATION_TYPE:
            case Constants.PREFERENCE_INSTALLATION_METHOD:
                SettingsActivity.shouldRestart = true;
                break;
        }
    }

    private void showNoRootDialog() {
        int backGroundColor = ViewUtil.getStyledAttribute(requireContext(), android.R.attr.colorBackground);
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setTitle(R.string.action_installations);
        builder.setMessage(R.string.pref_install_mode_no_root);
        builder.setPositiveButton(android.R.string.ok, (dialog, which) -> dialog.dismiss());
        builder.setBackground(new ColorDrawable(backGroundColor));
        builder.create();
        builder.show();
    }

    private void showNoServicesDialog() {
        int backGroundColor = ViewUtil.getStyledAttribute(requireContext(), android.R.attr.colorBackground);
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setTitle(R.string.action_installations);
        builder.setMessage(R.string.pref_install_mode_no_services);
        builder.setPositiveButton(android.R.string.ok, (dialog, which) -> dialog.dismiss());
        builder.setBackground(new ColorDrawable(backGroundColor));
        builder.create();
        builder.show();
    }

    @Override
    public void onDestroy() {
        try {
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
        } catch (Exception ignored) {
        }
        super.onDestroy();
    }
}