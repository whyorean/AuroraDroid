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

package com.aurora.adroid.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.aurora.adroid.Constants;
import com.aurora.adroid.installer.AppInstaller;

import org.apache.commons.lang3.StringUtils;

public class InstallReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle extras = intent.getExtras();
        if ((extras != null)) {
            final String packageName = extras.getString(Constants.INTENT_PACKAGE_NAME, "");
            final String fileUri = extras.getString(Constants.STRING_EXTRA, "");
            if (StringUtils.isNotEmpty(packageName) && StringUtils.isNotEmpty(fileUri)) {
                AppInstaller.getInstance(context)
                        .getDefaultInstaller()
                        .installApk(packageName, fileUri);
            }
        }
    }
}
