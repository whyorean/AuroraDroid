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
import android.content.Intent;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;

import io.reactivex.disposables.CompositeDisposable;

public abstract class InstallerBase implements IInstaller {

    protected final String INSTALL_PACKAGE_TEMPLATE = "pm install --user %s %s";
    protected final String UNINSTALL_PACKAGE_TEMPLATE = "pm uninstall %s";

    protected Context context;
    protected CompositeDisposable disposable = new CompositeDisposable();

    public InstallerBase(Context context) {
        this.context = context;
    }

    @Override
    public void uninstall(@NonNull String packageName) {
        Uri uri = Uri.fromParts("package", packageName, null);
        Intent intent = new Intent();
        intent.setData(uri);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            intent.setAction(Intent.ACTION_DELETE);
        } else {
            intent.setAction(Intent.ACTION_UNINSTALL_PACKAGE);
            intent.putExtra(Intent.EXTRA_RETURN_RESULT, true);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public enum Type {
        INSTALL,
        UNINSTALL
    }
}
