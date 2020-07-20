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

package com.aurora.adroid.installer;

import android.app.PendingIntent;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInstaller;

import androidx.annotation.NonNull;

import com.aurora.adroid.util.Log;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class SessionInstaller extends InstallerBase {

    public SessionInstaller(Context context) {
        super(context);
    }

    @Override
    public void installApk(@NonNull String packageName, @NonNull String filePath) {
        final File fileName = new File(filePath);
        xInstall(packageName, fileName);
    }

    @Override
    public void installApk(@NonNull String packageName, @NonNull File fileName) {
        xInstall(packageName, fileName);
    }

    private void xInstall(String packageName, File file) {
        final PackageInstaller packageInstaller = context.getPackageManager().getPackageInstaller();
        try {
            final PackageInstaller.SessionParams sessionParams = new PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL);
            final int sessionID = packageInstaller.createSession(sessionParams);
            final PackageInstaller.Session session = packageInstaller.openSession(sessionID);
            final InputStream inputStream = new FileInputStream(file);
            final OutputStream outputStream = session.openWrite(file.getName(), 0, file.length());

            IOUtils.copy(inputStream, outputStream);
            session.fsync(outputStream);
            inputStream.close();
            outputStream.close();

            final Intent callbackIntent = new Intent(context, InstallerService.class);
            final PendingIntent pendingIntent = PendingIntent.getService(
                    context,
                    sessionID,
                    callbackIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            session.commit(pendingIntent.getIntentSender());
            session.close();
        } catch (Exception e) {
            Log.e(e.getMessage());
        }
    }
}
