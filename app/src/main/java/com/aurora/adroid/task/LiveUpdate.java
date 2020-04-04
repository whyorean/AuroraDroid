package com.aurora.adroid.task;

import android.content.Context;

import com.aurora.adroid.AuroraApplication;
import com.aurora.adroid.download.DownloadManager;
import com.aurora.adroid.model.App;
import com.aurora.adroid.util.DatabaseUtil;
import com.aurora.adroid.util.Log;
import com.aurora.adroid.util.PathUtil;
import com.aurora.adroid.util.Util;
import com.tonyodev.fetch2.AbstractFetchGroupListener;
import com.tonyodev.fetch2.Download;
import com.tonyodev.fetch2.EnqueueAction;
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
        final String apkName = app.getAppPackage().getApkName();
        final Request request = new Request(DatabaseUtil.getDownloadURl(app), PathUtil.getApkPath(context, apkName));
        request.setEnqueueAction(EnqueueAction.REPLACE_EXISTING);
        request.setGroupId(app.getPackageName().hashCode());
        request.setTag(app.getPackageName());

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
                        AuroraApplication.getInstaller().install(app);
                    }
                    fetch.removeListener(this);
                }
            }
        };
    }
}
