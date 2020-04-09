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

import com.aurora.adroid.model.App;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;


public class ApkCopier {

    Context context;

    public ApkCopier(Context context) {
        this.context = context;
    }

    public boolean copy(App app) {
        File destination = PathUtil.getApkCopyPath(app.getPackageName());

        if (destination.exists()) {
            Log.i("%s exists", destination.toString());
            return true;
        }

        File currentApk = getCurrentApk(app);
        if (null == currentApk) {
            Log.e("applicationInfo.sourceDir is empty");
            return false;
        }

        if (!currentApk.exists()) {
            Log.e("%s does not exist", currentApk);
            return false;
        }
        return copy(currentApk, destination);
    }

    private boolean copy(File input, File output) {
        File dir = output.getParentFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }
        try {
            IOUtils.copy(new FileInputStream(input), new FileOutputStream(output));
            return true;
        } catch (IOException e) {
            Log.e("Error copying APK : %s", e.getMessage());
            return false;
        }
    }

    private File getCurrentApk(App app) {
        try {
            final PackageInfo packageInfo = context.getPackageManager().getPackageInfo(app.getPackageName(),
                    PackageManager.GET_META_DATA | PackageManager.GET_PERMISSIONS);
            if (packageInfo != null && packageInfo.applicationInfo != null) {
                return new File(packageInfo.applicationInfo.sourceDir);
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }
}
