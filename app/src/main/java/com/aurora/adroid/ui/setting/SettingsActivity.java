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

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.aurora.adroid.Constants;
import com.aurora.adroid.R;
import com.aurora.adroid.ui.generic.activity.BaseActivity;
import com.aurora.adroid.ui.view.MultiTextLayout;
import com.aurora.adroid.util.Util;
import com.aurora.adroid.util.ViewUtil;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SettingsActivity extends BaseActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static boolean shouldRestart = false;

    @BindView(R.id.action1)
    AppCompatImageView action1;
    @BindView(R.id.multi_text_layout)
    MultiTextLayout multiTextLayout;
    @BindView(R.id.action2)
    AppCompatImageView action2;

    private NavController navController;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);
        sharedPreferences = Util.getPrefs(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        navController = Navigation.findNavController(this, R.id.nav_host_preference);
        setupToolbar();
    }

    private void setupToolbar() {
        action1.setImageDrawable(getDrawable(R.drawable.ic_arrow_back));
        action1.setOnClickListener(v -> onBackPressed());
        multiTextLayout.setTxtPrimary(getString(R.string.string_aurora));
        multiTextLayout.setTxtSecondary(getString(R.string.menu_setting));
    }

    @Override
    public void onBackPressed() {
        if (!navController.popBackStack()) {
            super.onBackPressed();
        }

        if (shouldRestart)
            askRestart();
    }

    @Override
    protected void onDestroy() {
        try {
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
        } catch (Exception e) {

        }
        super.onDestroy();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case Constants.PREFERENCE_UI_THEME_2:
                ViewUtil.switchTheme(this);
                break;
        }
    }

    private void askRestart() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.action_restart))
                .setMessage(getString(R.string.pref_dialog_to_apply_restart))
                .setPositiveButton(getString(R.string.action_restart), (dialog, which) -> Util.restartApp(this))
                .setNegativeButton(getString(R.string.action_later), (dialog, which) -> dialog.dismiss());
        builder.create();
        builder.show();
        shouldRestart = false;
    }
}
