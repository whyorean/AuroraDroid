package com.aurora.adroid.section;

import android.content.Context;
import android.content.pm.PackageInfo;

import com.aurora.adroid.model.App;
import com.aurora.adroid.util.PackageUtil;

import java.util.List;

public class UpdatableAppSection extends InstalledAppSection {

    public UpdatableAppSection(Context context, List<App> appList) {
        super(context, appList);
    }

    private void getDetails(List<String> versionList, List<String> extraList, App app) {
        PackageInfo packageInfo = PackageUtil.getPackageInfo(packageManager, app.getPackageName());
        if (packageInfo != null)
            versionList.add(packageInfo.versionName + "." + packageInfo.versionCode);
        extraList.add(PackageUtil.isSystemApp(packageManager, app.getPackageName()) ?
                "System App"
                : "User App");
    }
}
