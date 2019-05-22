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

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SeekBarPreference;

import com.aurora.adroid.Constants;
import com.aurora.adroid.R;
import com.aurora.adroid.activity.SettingsActivity;
import com.aurora.adroid.util.PathUtil;
import com.aurora.adroid.util.PrefUtil;

import java.io.File;
import java.io.IOException;

public class DownloaderFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    private Context context;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        getPreferenceManager().setSharedPreferencesName(Constants.SHARED_PREFERENCES_KEY);
        setPreferencesFromResource(R.xml.preferences_downloader, rootKey);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupDownloadPath();
        setupActiveDownloads();

        ListPreference strategyList = findPreference(Constants.PREFERENCE_DOWNLOAD_STRATEGY);
        assert strategyList != null;
        strategyList.setOnPreferenceChangeListener((preference, newValue) -> {
            PrefUtil.putString(context, Constants.PREFERENCE_DOWNLOAD_STRATEGY, (String) newValue);
            return true;
        });
    }

    private void setupDownloadPath() {
        EditTextPreference editTextPreference = findPreference(Constants.PREFERENCE_DOWNLOAD_DIRECTORY);
        assert editTextPreference != null;
        editTextPreference.setText(PathUtil.getRootApkPath(context));
        editTextPreference.setSummaryProvider(preference -> PathUtil.getRootApkPath(context));
        editTextPreference.setOnPreferenceChangeListener((preference, newValue) -> {
            boolean success = checkIfValid(newValue.toString());
            if (success)
                PrefUtil.putString(context, Constants.PREFERENCE_DOWNLOAD_DIRECTORY, newValue.toString());
            else {
                PrefUtil.putString(context, Constants.PREFERENCE_DOWNLOAD_DIRECTORY, "");
                Toast.makeText(context, "Could not set download path", Toast.LENGTH_LONG).show();
            }
            return true;
        });
    }

    private void setupActiveDownloads() {
        SeekBarPreference seekBarPreference = findPreference(Constants.PREFERENCE_DOWNLOAD_ACTIVE);
        assert seekBarPreference != null;
        seekBarPreference.setShowSeekBarValue(true);
        seekBarPreference.setMin(1);
        seekBarPreference.setOnPreferenceChangeListener((preference, newValue) -> {
            int value = (Integer) newValue;
            PrefUtil.putInteger(context, Constants.PREFERENCE_DOWNLOAD_ACTIVE, value);
            SettingsActivity.shouldRestart = true;
            return true;
        });
    }

    private boolean checkIfValid(String newValue) {
        try {
            File newDir = new File(newValue).getCanonicalFile();
            if (newDir.exists()) {
                return newDir.canWrite();
            }
            if (context.checkCallingOrSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
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
                SettingsActivity.shouldRestart = true;
                break;
        }
    }
}
