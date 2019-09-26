package com.aurora.adroid.task;

import android.content.Context;
import android.content.ContextWrapper;

import com.aurora.adroid.AuroraApplication;
import com.aurora.adroid.download.DownloadManager;
import com.aurora.adroid.model.App;
import com.aurora.adroid.notification.GeneralNotification;
import com.aurora.adroid.util.DatabaseUtil;
import com.aurora.adroid.util.Log;
import com.aurora.adroid.util.PackageUtil;
import com.aurora.adroid.util.PathUtil;
import com.tonyodev.fetch2.AbstractFetchGroupListener;
import com.tonyodev.fetch2.Download;
import com.tonyodev.fetch2.EnqueueAction;
import com.tonyodev.fetch2.Error;
import com.tonyodev.fetch2.Fetch;
import com.tonyodev.fetch2.FetchGroup;
import com.tonyodev.fetch2.FetchListener;
import com.tonyodev.fetch2.Request;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class LiveUpdate extends ContextWrapper {

    private Context context;
    private App app;
    private Fetch fetch;
    private FetchListener fetchListener;
    private GeneralNotification notification;
    private int hashCode;

    public LiveUpdate(Context context, App app) {
        super(context);
        this.context = context;
        this.app = app;
        this.fetch = DownloadManager.getFetchInstance(context);
        this.fetchListener = getFetchListener();
        this.hashCode = app.getPackageName().hashCode();
        this.notification = new GeneralNotification(context, app);
    }

    public void enqueueUpdate() {
        final String apkName = app.getAppPackage().getApkName();
        final Request request = new Request(DatabaseUtil.getDownloadURl(app), PathUtil.getApkPath(this, apkName));
        request.setEnqueueAction(EnqueueAction.REPLACE_EXISTING);
        request.setGroupId(app.getPackageName().hashCode());
        request.setTag(app.getPackageName());

        List<Request> requestList = new ArrayList<>();
        requestList.add(request);

        fetch.addListener(fetchListener);

        fetch.enqueue(requestList, updatedRequestList ->
                Log.i("Downloading Apks : %s", app.getPackageName()));

        //Add <PackageName,DisplayName> and <PackageName,IconURL> to PseudoMaps
        PackageUtil.addToPseudoPackageMap(this, app.getPackageName(), app.getName());
        PackageUtil.addToPseudoURLMap(this, app.getPackageName(), DatabaseUtil.getImageUrl(app));
    }

    private FetchListener getFetchListener() {
        return new AbstractFetchGroupListener() {

            @Override
            public void onQueued(int groupId, @NotNull Download download, boolean waitingNetwork, @NotNull FetchGroup fetchGroup) {
                if (groupId == hashCode) {
                    notification.notifyQueued(hashCode);
                }
            }

            @Override
            public void onResumed(int groupId, @NotNull Download download, @NotNull FetchGroup fetchGroup) {
                if (groupId == hashCode) {
                    int progress = fetchGroup.getGroupDownloadProgress();
                    if (progress < 0)
                        progress = 0;
                    notification.notifyProgress(progress, 0, hashCode);
                }
            }

            @Override
            public void onProgress(int groupId, @NotNull Download download, long etaInMilliSeconds, long downloadedBytesPerSecond, @NotNull FetchGroup fetchGroup) {
                if (groupId == hashCode) {
                    final int progress = fetchGroup.getGroupDownloadProgress();
                    notification.notifyProgress(progress, downloadedBytesPerSecond, hashCode);
                }
            }

            @Override
            public void onError(int groupId, @NotNull Download download, @NotNull Error error,
                                @Nullable Throwable throwable, @NotNull FetchGroup fetchGroup) {
                if (groupId == hashCode) {
                    Log.e("Error updating %s", app.getName());
                }
            }

            @Override
            public void onCompleted(int groupId, @NotNull Download download, @NotNull FetchGroup fetchGroup) {
                if (groupId == hashCode && fetchGroup.getGroupDownloadProgress() == 100) {
                    notification.notifyCompleted();
                    AuroraApplication.getInstaller().install(app);
                    if (fetchListener != null) {
                        fetch.removeListener(fetchListener);
                        fetchListener = null;
                    }
                }
            }

            @Override
            public void onCancelled(int groupId, @NotNull Download download, @NotNull FetchGroup fetchGroup) {
                if (groupId == hashCode) {
                    Log.e("Cancelled %s", app.getName());
                }
            }
        };
    }
}
