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

package com.aurora.adroid.ui.intro;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.aurora.adroid.Constants;
import com.aurora.adroid.R;
import com.aurora.adroid.ui.generic.activity.BaseActivity;

import butterknife.ButterKnife;

public class IntroActivity extends BaseActivity {

    private NavController navController;
    private int launchMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
        ButterKnife.bind(this);

        navController = Navigation.findNavController(this, R.id.nav_host_intro);

        Intent intent = getIntent();
        if (intent != null)
            onNewIntent(intent);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        /*
         * 1. For Complete Intro
         * 2. For Permissions only
         * 3. For Repo only
         */
        launchMode = intent.getIntExtra(Constants.INT_EXTRA, 1);
        switch (launchMode) {
            case 2:
                navController.navigate(R.id.permissionFragment);
                break;
            case 3:
                navController.navigate(R.id.repoFragment);
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1337) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                navController.navigate(R.id.action_permission_to_repo);
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (launchMode == 2 || launchMode == 3)
            super.onBackPressed();

        if (!navController.popBackStack())
            super.onBackPressed();
    }
}
