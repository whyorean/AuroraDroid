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

package com.aurora.adroid.ui.generic.activity;

import android.content.res.Configuration;
import android.os.Bundle;
import android.transition.Explode;
import android.view.Window;

import androidx.appcompat.app.AppCompatActivity;

import com.aurora.adroid.manager.LocaleManager;
import com.aurora.adroid.util.Log;
import com.aurora.adroid.util.ViewUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Modifier;

public abstract class BaseActivity extends AppCompatActivity {

    protected int intExtra;
    protected String stringExtra;
    protected Gson gson = new GsonBuilder().excludeFieldsWithModifiers(Modifier.TRANSIENT).create();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new LocaleManager(this).setLocale();
        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        switch (currentNightMode) {
            case Configuration.UI_MODE_NIGHT_NO:
                Log.d("Light Theme Applied");
                ViewUtil.setLightStatusBar(this);
                break;
            case Configuration.UI_MODE_NIGHT_YES:
                Log.d("Dark Theme Applied");
                break;
        }
    }
}
