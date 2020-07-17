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

package com.aurora.adroid.task;

import android.content.Context;

import com.aurora.adroid.download.DownloadManager;
import com.aurora.adroid.download.RequestBuilder;
import com.aurora.adroid.installer.AppInstaller;
import com.aurora.adroid.model.App;
import com.aurora.adroid.util.Log;
import com.aurora.adroid.util.Util;
import com.tonyodev.fetch2.AbstractFetchGroupListener;
import com.tonyodev.fetch2.Download;
import com.tonyodev.fetch2.Fetch;
import com.tonyodev.fetch2.FetchGroup;
import com.tonyodev.fetch2.FetchListener;
import com.tonyodev.fetch2.Request;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class LiveUpdate {

    private Context context;
    private App app;
    private Fetch fetch;
    private FetchListener fetchListener;
    private int hashCode;

    public LiveUpdate(Context context, App app) {
        this.context = context;
        this.app = app;
        this.fetch = DownloadManager.getFetchInstance(context);
        this.fetchListener = getFetchListener();
        this.hashCode = app.getPackageName().hashCode();
    }

    public void enqueueUpdate() {
        final Request request = RequestBuilder.buildRequest(context, app);
        final List<Request> requestList = new ArrayList<>();
        requestList.add(request);

        fetch.addListener(fetchListener);
        fetch.enqueue(requestList, updatedRequestList -> Log.i("Downloading App : %s", app.getPackageName()));
    }

    private FetchListener getFetchListener() {
        return new AbstractFetchGroupListener() {
            @Override
            public void onCompleted(int groupId, @NotNull Download download, @NotNull FetchGroup fetchGroup) {
                if (groupId == hashCode && fetchGroup.getGroupDownloadProgress() == 100) {
                    if (Util.shouldAutoInstallApk(context)) {
                        //Call the installer
                        AppInstaller.getInstance(context)
                                .getDefaultInstaller()
                                .installApk(app.getPackageName(), download.getFile());
                    }
                    fetch.removeListener(this);
                }
            }
        };
    }
}
