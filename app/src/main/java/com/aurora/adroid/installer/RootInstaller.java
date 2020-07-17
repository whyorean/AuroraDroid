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

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.aurora.adroid.AuroraApplication;
import com.aurora.adroid.R;
import com.aurora.adroid.event.Event;
import com.aurora.adroid.event.EventType;
import com.aurora.adroid.util.Log;
import com.aurora.adroid.util.Util;
import com.topjohnwu.superuser.Shell;

import org.apache.commons.lang3.StringUtils;

import java.io.File;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class RootInstaller extends InstallerBase {

    public RootInstaller(Context context) {
        super(context);
    }

    @Override
    public void installApk(@NonNull String packageName, @NonNull String filePath) {
        if (Shell.getShell().isRoot()) {
            String command = String.format(INSTALL_PACKAGE_TEMPLATE, Util.getInstallationProfile(context), filePath);
            disposable.add(Observable.fromCallable(() -> Shell.su(command).exec())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(result -> iLog(result, packageName, Type.INSTALL), throwable -> {
                        Toast.makeText(context, R.string.string_install_failed, Toast.LENGTH_SHORT).show();
                        eLog(packageName, Type.INSTALL);
                    }));
        } else {
            notifyNoRoot(packageName);
        }
    }

    @Override
    public void installApk(@NonNull String packageName, @NonNull File fileName) {
        if (Shell.getShell().isRoot()) {
            String command = String.format(INSTALL_PACKAGE_TEMPLATE, Util.getInstallationProfile(context), fileName.getAbsolutePath());
            disposable.add(Observable.fromCallable(() -> Shell.su(command).exec())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(result -> iLog(result, packageName, Type.INSTALL), throwable -> {
                        Toast.makeText(context, R.string.string_install_failed, Toast.LENGTH_SHORT).show();
                        eLog(packageName, Type.INSTALL);
                    }));
        } else {
            notifyNoRoot(packageName);
        }
    }

   /* @Override
    public void uninstall(@NonNull String packageName) {
        if (Shell.getShell().isRoot()) {
            String command = String.format(UNINSTALL_PACKAGE_TEMPLATE, packageName);
            disposable.add(Observable.fromCallable(() -> Shell.su(command).exec())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(result -> iLog(result, packageName, Type.UNINSTALL), throwable -> {
                        Toast.makeText(context, R.string.string_uninstall_failed, Toast.LENGTH_SHORT).show();
                        eLog(packageName, Type.UNINSTALL);
                    }));
        } else {
            notifyNoRoot(packageName);
        }
    }*/

    private void iLog(Shell.Result result, String packageName, Type type) {
        if (result.isSuccess())
            Log.i(StringUtils.joinWith(StringUtils.SPACE, context.getString(type == Type.INSTALL
                    ? R.string.string_install_success
                    : R.string.string_uninstall_success), packageName));
        else
            eLog(packageName, type);
    }

    private void eLog(String packageName, Type type) {
        Log.e(StringUtils.joinWith(StringUtils.SPACE, context.getString(type == Type.INSTALL
                ? R.string.string_install_failed
                : R.string.string_uninstall_failed), packageName));
    }

    private void notifyNoRoot(String packageName) {
        AuroraApplication.rxNotify(new Event(EventType.NO_ROOT, packageName));
        Toast.makeText(context, R.string.string_no_root, Toast.LENGTH_SHORT).show();
    }
}
