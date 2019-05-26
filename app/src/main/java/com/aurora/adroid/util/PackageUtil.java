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

package com.aurora.adroid.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import com.aurora.adroid.ArchType;
import com.aurora.adroid.model.App;
import com.aurora.adroid.model.Package;

import java.util.List;

public class PackageUtil {

    public static boolean isInstalledVersion(Context context, Package pkg) {
        try {
            context.getPackageManager().getPackageInfo(pkg.getPackageName(), 0);
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(pkg.getPackageName(),
                    PackageManager.GET_META_DATA);
            return packageInfo.versionCode == pkg.getVersionCode()
                    && packageInfo.versionName.equals(pkg.getVersionName());
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static boolean isInstalled(Context context, String packageName) {
        try {
            context.getPackageManager().getPackageInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static App getAppByPackage(PackageManager packageManager, String packageName) {
        try {
            final PackageInfo packageInfo = packageManager.getPackageInfo(packageName,
                    PackageManager.GET_META_DATA | PackageManager.GET_PERMISSIONS);
            final App app = new App(packageInfo);
            app.setPackageName(packageInfo.packageName);
            app.setName(packageManager.getApplicationLabel(packageInfo.applicationInfo).toString());
            app.setIconDrawable(packageManager.getApplicationIcon(app.getPackageName()));
            return app;
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    public static PackageInfo getPackageInfo(PackageManager packageManager, String packageName) {
        try {
            return packageManager.getPackageInfo(packageName,
                    PackageManager.GET_META_DATA | PackageManager.GET_PERMISSIONS);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    public static boolean isArchSpecificPackage(Package pkg) {
        final String apkName = pkg.getApkName().toLowerCase();
        return apkName.contains("arm")
                || apkName.contains("armeabi-v7a")
                || apkName.contains("arm64")
                || apkName.contains("arm64-v8a")
                || apkName.contains("x86")
                || apkName.contains("x86-64");
    }

    public static ArchType getPackageArch(Package pkg) {
        final String apkName = pkg.getApkName().toLowerCase();
        if (apkName.contains("arm64-v8a") || apkName.contains("arm64"))
            return ArchType.ARM64;
        else if (apkName.contains("armeabi-v7a") || apkName.contains("arm"))
            return ArchType.ARM;
        else if (apkName.contains("x86-64"))
            return ArchType.x86_64;
        else if (apkName.contains("x86"))
            return ArchType.x86;
        else
            return ArchType.ARM;
    }

    public static ArchType getSystemArch() {
        switch (Build.SUPPORTED_ABIS[0]) {
            case "arm64-v8a":
                return ArchType.ARM64;
            case "armeabi-v7a":
                return ArchType.ARM;
            case "x86-64":
                return ArchType.x86_64;
            case "x86":
                return ArchType.x86;
            default:
                return ArchType.ARM;
        }
    }

    public static boolean isSdkCompatible(Package pkg) {
        return Build.VERSION.SDK_INT >= Util.parseInt(pkg.getMinSdkVersion(), 21);
    }

    public static boolean isSupportedPackage(Package pkg) {
        boolean archSpecific = isArchSpecificPackage(pkg);
        boolean sdkCompatible = isSdkCompatible(pkg);
        if (!archSpecific && sdkCompatible)
            return true;
        ArchType pkgArch = getPackageArch(pkg);
        ArchType systemArch = getSystemArch();
        if (pkgArch == ArchType.ARM64 && systemArch == ArchType.ARM64 && sdkCompatible)
            return true;
        else if (pkgArch == ArchType.ARM && (systemArch == ArchType.ARM || systemArch == ArchType.ARM64) && sdkCompatible)
            return true;
        if (pkgArch == ArchType.x86_64 && systemArch == ArchType.x86_64)
            return true;
        else if (pkgArch == ArchType.x86 && (systemArch == ArchType.x86 || systemArch == ArchType.x86_64) && sdkCompatible)
            return true;
        else
            return false;
    }

    public static Package getOptimumPackage(List<Package> packageList) {
        for (Package pkg : packageList) {
            if (isSupportedPackage(pkg))
                return pkg;
        }
        return packageList.get(0);
    }
}
