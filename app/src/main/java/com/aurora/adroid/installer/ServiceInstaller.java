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

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;

import androidx.annotation.NonNull;

import com.aurora.adroid.BuildConfig;
import com.aurora.adroid.Constants;
import com.aurora.adroid.util.Log;
import com.aurora.services.IPrivilegedCallback;
import com.aurora.services.IPrivilegedService;

import java.io.File;

public class ServiceInstaller extends InstallerBase {

    private static final int ACTION_INSTALL_REPLACE_EXISTING = 2;

    public ServiceInstaller(Context context) {
        super(context);
    }

    @Override
    public void installApk(@NonNull String packageName, @NonNull String filePath) {
        xInstall(packageName, Uri.fromFile(new File(filePath)));
    }

    @Override
    public void installApk(@NonNull String packageName, @NonNull File fileName) {
        xInstall(packageName, Uri.fromFile(fileName));
    }


    private void xInstall(@NonNull String packageName, @NonNull Uri fileUri) {
        final ServiceConnection serviceConnection = new ServiceConnection() {
            public void onServiceConnected(ComponentName name, IBinder binder) {
                IPrivilegedService service = IPrivilegedService.Stub.asInterface(binder);
                IPrivilegedCallback callback = new IPrivilegedCallback.Stub() {
                    @Override
                    public void handleResult(String packageName, int returnCode) {

                    }
                };
                try {
                    service.installPackage(
                            fileUri,
                            ACTION_INSTALL_REPLACE_EXISTING,
                            BuildConfig.APPLICATION_ID,
                            callback
                    );
                } catch (RemoteException e) {
                    Log.e("Connecting to privileged service failed");
                }
            }

            public void onServiceDisconnected(ComponentName name) {
                Log.e("Disconnected from privileged service");
            }
        };

        final Intent serviceIntent = new Intent(Constants.PRIVILEGED_EXTENSION_SERVICE_INTENT);
        serviceIntent.setPackage(Constants.PRIVILEGED_EXTENSION_PACKAGE_NAME);
        context.getApplicationContext().bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }
}
