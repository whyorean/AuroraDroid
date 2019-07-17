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
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;

import com.aurora.adroid.ArchType;
import com.aurora.adroid.model.App;
import com.aurora.adroid.model.Package;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PackageUtil {

    private static final List<String> archList = new ArrayList<>();
    private static final String PSEUDO_PACKAGE_MAP = "PSEUDO_PACKAGE_MAP";
    private static final String PSEUDO_URL_MAP = "PSEUDO_URL_MAP";

    static {
        archList.add("arm64-v8a");
        archList.add("armeabi-v7a");
        archList.add("x86");
    }

    public static String getAppDisplayName(Context context, String packageName) {
        Map<String, String> pseudoMap = getPseudoPackageMap(context);
        return TextUtil.emptyIfNull(pseudoMap.get(packageName));
    }

    public static String getIconURL(Context context, String packageName) {
        Map<String, String> pseudoMap = getPseudoURLMap(context);
        return TextUtil.emptyIfNull(pseudoMap.get(packageName));
    }

    private static Map<String, String> getPseudoPackageMap(Context context) {
        return PrefUtil.getMap(context, PSEUDO_PACKAGE_MAP);
    }

    private static Map<String, String> getPseudoURLMap(Context context) {
        return PrefUtil.getMap(context, PSEUDO_URL_MAP);
    }

    public static void addToPseudoPackageMap(Context context, String packageName, String displayName) {
        Map<String, String> pseudoMap = getPseudoPackageMap(context);
        pseudoMap.put(packageName, displayName);
        PrefUtil.saveMap(context, pseudoMap, PSEUDO_PACKAGE_MAP);
    }

    public static void addToPseudoURLMap(Context context, String packageName, String iconURL) {
        Map<String, String> pseudoMap = getPseudoURLMap(context);
        pseudoMap.put(packageName, iconURL);
        PrefUtil.saveMap(context, pseudoMap, PSEUDO_URL_MAP);
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

    public static boolean isSystemApp(PackageManager packageManager, String packageName) {
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(packageName, 0);
            return (packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
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

    @NonNull
    public static String getDisplayName(Context context, String packageName) {
        try {
            PackageManager packageManager = context.getPackageManager();
            ApplicationInfo appInfo = packageManager.getApplicationInfo(packageName, 0);
            return packageManager.getApplicationLabel(appInfo).toString();
        } catch (Exception e) {
            return "";
        }
    }

    public static boolean isArchSpecificPackage(Package pkg) {
        return !pkg.getNativecode().isEmpty() && !pkg.getNativecode().containsAll(archList);
    }


    public static String getPackageArchName(Package pkg) {
        if (!isArchSpecificPackage(pkg))
            return "Universal";
        else
            return pkg.getNativecode().get(0);
    }

    private static ArchType getArchFromNativeCode(String nativeCode) {
        switch (nativeCode) {
            case "arm64-v8a":
            case "arm64":
                return ArchType.ARM64;
            case "armeabi-v7a":
            case "armeabi":
                return ArchType.ARM;
            case "x86-64":
                return ArchType.x86_64;
            case "x86":
                return ArchType.x86;
            default:
                return ArchType.UNIVERSAL;
        }
    }

    private static ArchType getSystemArch() {
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

    private static ArchType getAltSystemArch() {
        switch (Build.SUPPORTED_ABIS[0]) {
            case "arm64-v8a":
            case "armeabi-v7a":
                return ArchType.ARM;
            case "x86-64":
            case "x86":
                return ArchType.x86;
            default:
                return ArchType.ARM;
        }
    }

    private static boolean isSdkCompatible(Package pkg) {
        return Build.VERSION.SDK_INT >= Util.parseInt(pkg.getMinSdkVersion(), 21);
    }

    public static boolean isBestFitSupportedPackage(Package pkg) {
        if (!isSdkCompatible(pkg))
            return false;
        if (!isArchSpecificPackage(pkg))
            return true;

        final List<String> nativeCodeList = pkg.getNativecode();
        final ArchType pkgArch = getArchFromNativeCode(nativeCodeList.get(0));
        final ArchType systemArch = getSystemArch();
        return pkgArch == systemArch;
    }

    public static boolean isSupportedPackage(Package pkg) {
        if (!isSdkCompatible(pkg))
            return false;
        if (!isArchSpecificPackage(pkg))
            return true;

        final List<String> nativeCodeList = pkg.getNativecode();
        final ArchType pkgArch = getArchFromNativeCode(nativeCodeList.get(0));
        final ArchType systemArch = getSystemArch();
        final ArchType systemArch2 = getAltSystemArch();
        return pkgArch == systemArch || pkgArch == systemArch2;
    }

    public static Package getOptimumPackage(List<Package> packageList) {
        for (Package pkg : packageList) {
            if (isBestFitSupportedPackage(pkg))
                return pkg;
        }

        for (Package pkg : packageList) {
            if (isSupportedPackage(pkg))
                return pkg;
        }

        for (Package pkg : packageList) {
            final ArchType pkgArch = getArchFromNativeCode(pkg.getNativecode().get(0));
            final ArchType systemArch = getAltSystemArch();
            if (pkgArch == systemArch)
                return pkg;
        }
        return packageList.get(0);
    }
}
