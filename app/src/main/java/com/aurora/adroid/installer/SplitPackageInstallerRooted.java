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

package com.aurora.adroid.installer;

import android.content.Context;
import android.content.pm.PackageInfo;

import com.aurora.adroid.InstallationStatus;
import com.aurora.adroid.util.Log;
import com.aurora.adroid.util.Root;

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SplitPackageInstallerRooted extends SplitPackageInstaller {

    private Root root;

    public SplitPackageInstallerRooted(Context context) {
        super(context);
        root = new Root();
    }

    @Override
    protected void installApkFiles(List<File> apkFiles) {
        try {
            if (root.isTerminated() || !root.isAcquired()) {
                root = new Root();
                if (!root.isAcquired()) {
                    dispatchCurrentSessionUpdate(InstallationStatus.INSTALLATION_FAILED,
                            "No Root Available");
                    installationCompleted();
                    return;
                }
            }

            int totalSize = 0;
            for (File apkFile : apkFiles)
                totalSize += apkFile.length();

            String result = ensureCommandSucceeded(root.exec(String.format(Locale.getDefault(),
                    "pm install-create -i com.android.vending -r -S %d",
                    totalSize)));

            Pattern sessionIdPattern = Pattern.compile("(\\d+)");
            Matcher sessionIdMatcher = sessionIdPattern.matcher(result);
            boolean found = sessionIdMatcher.find();
            int sessionId = Integer.parseInt(sessionIdMatcher.group(1));

            for (File apkFile : apkFiles)
                ensureCommandSucceeded(root.exec(String.format(Locale.getDefault(),
                        "cat \"%s\" | pm install-write -S %d %d \"%s\"",
                        apkFile.getAbsolutePath(),
                        apkFile.length(),
                        sessionId,
                        apkFile.getName())));

            result = ensureCommandSucceeded(root.exec(String.format(Locale.getDefault(),
                    "pm install-commit %d ",
                    sessionId)));

            if (result.toLowerCase().contains("success"))
                dispatchCurrentSessionUpdate(InstallationStatus.INSTALLATION_SUCCEED,
                        getPackageNameFromApk(apkFiles));
            else
                dispatchCurrentSessionUpdate(InstallationStatus.INSTALLATION_FAILED,
                        String.format(Locale.getDefault(), "Error Root : %s", result));

            installationCompleted();
        } catch (Exception e) {
            Log.w(e.getMessage());
        }
    }

    private String ensureCommandSucceeded(String result) {
        if (result == null || result.length() == 0)
            throw new RuntimeException(root.readError());
        return result;
    }

    private String getPackageNameFromApk(List<File> apkFiles) {
        for (File apkFile : apkFiles) {
            PackageInfo packageInfo = getContext().getPackageManager()
                    .getPackageArchiveInfo(apkFile.getAbsolutePath(), 0);
            if (packageInfo != null)
                return packageInfo.packageName;
        }
        return "null";
    }
}
