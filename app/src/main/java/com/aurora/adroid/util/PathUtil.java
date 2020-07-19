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
import android.os.Build;
import android.os.Environment;

import com.aurora.adroid.Constants;

import java.io.File;

public class PathUtil {

    public static String getRepoDirectory(Context context) {
        final File dir = new File(context.getFilesDir().getPath() + "/repositories/");
        if (!dir.exists())
            dir.mkdir();
        return dir.getPath() + "/";
    }

    static public String getRootApkPath(Context context) {
        if (isCustomPath(context))
            return PrefUtil.getString(context, Constants.PREFERENCE_DOWNLOAD_DIRECTORY);
        else
            return getBaseApkDirectory(context);
    }

    static public File getApkCopyPath(String apkName) {
        return new File(getBaseCopyDirectory(), apkName + ".apk");
    }

    public static String getBaseApkDirectory(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && Util.isRootInstallEnabled(context)) {
            return context.getFilesDir().getPath();
        } else
            return getExtBaseDirectory();
    }

    public static String getApkPath(Context context, String packageName, long versionCode) {
        return getRootApkPath(context) + "/" + packageName + "." + versionCode + ".apk";
    }

    public static boolean fileExists(Context context, String packageName, long versionCode) {
        return new File(getApkPath(context, packageName, versionCode)).exists();
    }

    public static synchronized void deleteRepoFiles(Context context, String fileName) {
        File[] files = new File(getRepoDirectory(context)).listFiles();
        if (files == null)
            return;
        for (File file : files)
            if (file.getName().startsWith(fileName))
                file.delete();
    }

    public static synchronized void deleteApkFile(Context context, String fileName) {
        File[] files = new File(getRootApkPath(context)).listFiles();
        if (files == null)
            return;
        for (File file : files)
            if (file.getName().startsWith(fileName))
                file.delete();
    }

    static private boolean isCustomPath(Context context) {
        return (!getCustomPath(context).isEmpty());
    }

    static public String getCustomPath(Context context) {
        return PrefUtil.getString(context, Constants.PREFERENCE_DOWNLOAD_DIRECTORY);
    }

    static public String getExtBaseDirectory() {
        return Environment.getExternalStorageDirectory().getPath() + "/Aurora/Droid";
    }

    static public String getBaseFilesDirectory() {
        return getExtBaseDirectory() + "/Files";
    }

    static public String getBaseCopyDirectory() {
        return getBaseFilesDirectory() + "/APK";
    }
}
