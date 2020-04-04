package com.aurora.adroid.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.aurora.adroid.AuroraApplication;
import com.aurora.adroid.event.Event;
import com.aurora.adroid.event.EventType;
import com.aurora.adroid.model.App;
import com.aurora.adroid.task.LiveUpdate;
import com.aurora.adroid.util.Log;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class BulkUpdateService extends Service {

    public static BulkUpdateService instance = null;

    private List<App> appList = new ArrayList<>();

    public static boolean isServiceRunning() {
        try {
            return instance != null && instance.isRunning();
        } catch (NullPointerException e) {
            return false;
        }
    }

    private boolean isRunning() {
        return true;
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        appList = AuroraApplication.getOngoingUpdateList();
        updateAllApps();
    }

    private void updateAllApps() {
        AuroraApplication.setBulkUpdateAlive(true);
        AuroraApplication.rxNotify(new Event(EventType.BULK_UPDATE_NOTIFY));
        Observable.fromIterable(appList)
                .subscribeOn(Schedulers.io())
                .doOnNext(app -> new LiveUpdate(this, app).enqueueUpdate())
                .doOnComplete(() -> {
                })
                .subscribe();
    }

    @Override
    public void onDestroy() {
        AuroraApplication.setBulkUpdateAlive(false);
        AuroraApplication.rxNotify(new Event(EventType.BULK_UPDATE_NOTIFY));
        instance = null;
        super.onDestroy();
    }
}
