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
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.Nullable;

import com.aurora.adroid.model.App;
import com.aurora.adroid.model.Package;

import java.util.ArrayList;
import java.util.List;

public class PackageUtil {

    private static final String ACTION_PACKAGE_REPLACED_NON_SYSTEM = "ACTION_PACKAGE_REPLACED_NON_SYSTEM";
    private static final String ACTION_PACKAGE_INSTALLATION_FAILED = "ACTION_PACKAGE_INSTALLATION_FAILED";
    private static final String ACTION_UNINSTALL_PACKAGE_FAILED = "ACTION_UNINSTALL_PACKAGE_FAILED";

    private static final List<String> archList = new ArrayList<>();

    static {
        archList.add("arm64-v8a");
        archList.add("armeabi-v7a");
        archList.add("x86");
    }

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

    public static PackageInfo getPackageInfo(PackageManager packageManager, String packageName) {
        try {
            return packageManager.getPackageInfo(packageName,
                    PackageManager.GET_META_DATA | PackageManager.GET_PERMISSIONS);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    public static App getAppFromPackageName(PackageManager packageManager, String packageName, boolean extended) {
        try {
            final App app = new App();
            final PackageInfo packageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_META_DATA);
            app.setPackageName(packageName);
            app.setName(packageManager.getApplicationLabel(packageInfo.applicationInfo).toString());
            app.setSuggestedVersionName(packageInfo.versionName);
            app.setSuggestedVersionCode(packageInfo.versionCode);
            if (extended) {
                app.setSystemApp((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0);
                app.setIconDrawable(packageManager.getApplicationIcon(packageName));
            }
            return app;
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    public static boolean isArchSpecificPackage(Package pkg) {
        if (pkg.getNativecode() == null)
            return false;
        else
            return !pkg.getNativecode().isEmpty() && !pkg.getNativecode().containsAll(archList);
    }


    public static String getPackageArchName(Package pkg) {
        if (!isArchSpecificPackage(pkg))
            return "Universal";
        else
            return pkg.getNativecode().get(0);
    }

    public static boolean compatibleApi(@Nullable List<String> nativecode) {
        final String[] supportedAbis = Build.SUPPORTED_ABIS;

        if (nativecode == null) {
            return true;
        }

        for (final String cpuAbi : supportedAbis) {
            for (String code : nativecode) {
                if (code.equals(cpuAbi)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static List<Package> markCompatiblePackages(List<Package> packageList, String signer, boolean verifySigner) {

        final List<Package> packages = new ArrayList<>();
        final List<Package> compatiblePackages = new ArrayList<>();

        //Filter packages that do not match signer
        if (verifySigner) {
            for (Package pkg : packageList) {
                if (pkg.getSigner().equals(signer)) {
                    packages.add(pkg);
                }
            }
        } else {
            packages.addAll(packageList);
        }

        if (packages.isEmpty())
            return null;

        for (Package pkg : packages) {
            pkg.setCompatible(compatibleApi(pkg.getNativecode()));
            compatiblePackages.add(pkg);
        }

        return compatiblePackages;
    }

    public static boolean isBeta(String versionName) {
        return versionName.toLowerCase().contains("beta");
    }

    public static boolean isAlpha(String versionName) {
        return versionName.toLowerCase().contains("alpha");
    }

    public static boolean isStable(String versionName) {
        return !isBeta(versionName) && !isAlpha(versionName);
    }

    public static boolean isUpdatableVersion(Context context, Package pkg, PackageInfo packageInfo) {
        if (pkg.getVersionCode() > packageInfo.versionCode) {
            if (Util.isExperimentalUpdatesEnabled(context))
                return true;
            if (isAlpha(packageInfo.versionName) && (isAlpha(pkg.getVersionName()) || isBeta(pkg.getVersionName()) || isStable(pkg.getVersionName())))
                return true;
            else if (isBeta(packageInfo.versionName) && (isBeta(pkg.getVersionName()) || isStable(pkg.getVersionName())))
                return true;
            else return isStable(packageInfo.versionName) && isStable(pkg.getVersionName());
        }
        return false;
    }

    public static boolean isSuggestedUpdatableVersion(PackageInfo packageInfo, App app, Package pkg) {
        return  (pkg.isCompatible() && packageInfo.versionCode <= app.getSuggestedVersionCode() && app.getSuggestedVersionCode() == pkg.getVersionCode());
    }


    public static IntentFilter getFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addDataScheme("package");
        filter.addAction(Intent.ACTION_PACKAGE_INSTALL);
        filter.addAction(Intent.ACTION_PACKAGE_ADDED);
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        return filter;
    }
}
