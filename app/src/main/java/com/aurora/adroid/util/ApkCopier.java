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

package com.aurora.adroid.util;


import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import org.apache.commons.io.FileUtils;

import java.io.File;


public class ApkCopier {

    private Context context;

    public ApkCopier(Context context) {
        this.context = context;
    }

    public boolean copy(String packageName) {
        final File destination = PathUtil.getApkCopyPath(packageName);
        if (destination.exists()) {
            Log.i("Ignored, local copy already exists", destination.toString());
            return true;
        } else {
            final File currentApk = getCurrentApk(packageName);
            if (currentApk == null || !currentApk.exists()) {
                Log.e("No associated APK(s) found");
                return false;
            } else {
                return copy(currentApk, destination);
            }
        }
    }

    private boolean copy(File input, File output) {
        final File dir = output.getParentFile();

        /*Create parent directory, if not existing*/
        if (!dir.exists()) {
            dir.mkdirs();
        }

        try {
            FileUtils.copyFile(input, output);
            return true;
        } catch (Exception e) {
            Log.e("Error copying APK : %s", e.getMessage());
            return false;
        }
    }

    private File getCurrentApk(String packageName) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(packageName, 0);
            if (packageInfo != null && packageInfo.applicationInfo != null) {
                return new File(packageInfo.applicationInfo.sourceDir);
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
