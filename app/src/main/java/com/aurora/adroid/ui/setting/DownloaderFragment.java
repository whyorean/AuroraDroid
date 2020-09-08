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

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import com.aurora.adroid.Constants;
import com.aurora.adroid.R;
import com.aurora.adroid.util.PathUtil;
import com.aurora.adroid.util.PrefUtil;
import com.aurora.adroid.util.Util;
import com.aurora.filepicker.view.FilePickerPreference;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;

public class DownloaderFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    private SharedPreferences sharedPreferences;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences_downloader, rootKey);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sharedPreferences = Util.getPrefs(requireContext());
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);


        FilePickerPreference filePickerPreference = findPreference(Constants.PREFERENCE_DOWNLOAD_DIRECTORY);
        if (filePickerPreference != null)
            filePickerPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                String value = String.valueOf(newValue);
                String[] selections = value.split(FilePickerPreference.SEPARATOR);
                String selectedDir = selections[0];
                if (StringUtils.isNotEmpty(selectedDir)) {
                    boolean success = checkIfValid(selectedDir);
                    if (success) {
                        PrefUtil.putString(requireContext(), Constants.PREFERENCE_DOWNLOAD_DIRECTORY, newValue.toString());
                        preference.setSummary(selectedDir);
                    } else {
                        PrefUtil.putString(requireContext(), Constants.PREFERENCE_DOWNLOAD_DIRECTORY, "");
                        Toast.makeText(requireContext(), "Could not set download path", Toast.LENGTH_LONG).show();
                    }
                }
                return false;
            });

        SwitchPreferenceCompat switchPreference = findPreference(Constants.PREFERENCE_DOWNLOAD_INTERNAL);
        if (switchPreference != null) {
            switchPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                if (newValue.toString().equals("true"))
                    PrefUtil.putString(requireContext(), Constants.PREFERENCE_DOWNLOAD_DIRECTORY, requireContext().getFilesDir().getPath());
                else
                    PrefUtil.putString(requireContext(), Constants.PREFERENCE_DOWNLOAD_DIRECTORY, PathUtil.getExtBaseDirectory());
                return true;
            });
        }
    }

    private boolean checkIfValid(String newValue) {
        try {
            File newDir = new File(newValue).getCanonicalFile();
            if (newDir.exists()) {
                return newDir.canWrite();
            }
            if (requireContext().checkCallingOrSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                return newDir.mkdirs();
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case Constants.PREFERENCE_DOWNLOAD_STRATEGY:
            case Constants.PREFERENCE_DOWNLOAD_ACTIVE:
                SettingsActivity.shouldRestart = true;
                break;
        }
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
