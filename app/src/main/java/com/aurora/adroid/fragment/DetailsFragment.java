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

package com.aurora.adroid.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;

import com.aurora.adroid.R;
import com.aurora.adroid.event.Event;
import com.aurora.adroid.event.Events;
import com.aurora.adroid.event.RxBus;
import com.aurora.adroid.fragment.details.AppActionDetails;
import com.aurora.adroid.fragment.details.AppInfoDetails;
import com.aurora.adroid.fragment.details.AppLinkDetails;
import com.aurora.adroid.fragment.details.AppPackages;
import com.aurora.adroid.fragment.details.AppScreenshotsDetails;
import com.aurora.adroid.fragment.details.AppSubInfoDetails;
import com.aurora.adroid.model.App;
import com.aurora.adroid.receiver.DetailsInstallReceiver;
import com.aurora.adroid.task.FetchAppsTask;
import com.aurora.adroid.util.ContextUtil;
import com.aurora.adroid.util.Log;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class DetailsFragment extends Fragment {
    public static App app;

    @BindView(R.id.coordinator)
    CoordinatorLayout coordinatorLayout;

    private Context context;
    private String packageName;
    private CompositeDisposable disposable = new CompositeDisposable();
    private DetailsInstallReceiver detailsInstallReceiver;
    private AppActionDetails appActionDetails;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;

        disposable.add(RxBus.get().toObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(event -> {
                    if (event instanceof Event) {
                        Events eventEnum = ((Event) event).getEvent();
                        switch (eventEnum) {
                            case DOWNLOAD_INITIATED:
                                ContextUtil.runOnUiThread(() -> notifyAction(getString(R.string.download_progress)));
                                break;
                            case DOWNLOAD_FAILED:
                                ContextUtil.runOnUiThread(() -> notifyAction(getString(R.string.download_failed)));
                                break;
                            case DOWNLOAD_CANCELLED:
                                ContextUtil.runOnUiThread(() -> notifyAction(getString(R.string.download_canceled)));
                                break;
                            case DOWNLOAD_COMPLETED:
                                ContextUtil.runOnUiThread(() -> notifyAction(getString(R.string.download_completed)));
                                break;
                        }
                    }
                }));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_details, container, false);
        ButterKnife.bind(this, view);
        Bundle arguments = getArguments();
        if (arguments != null) {
            packageName = arguments.getString("PackageName");
        }
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fetchApp();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        detailsInstallReceiver = new DetailsInstallReceiver(packageName);
    }

    @Override
    public void onResume() {
        super.onResume();
        context.registerReceiver(detailsInstallReceiver, detailsInstallReceiver.getFilter());
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            context.unregisterReceiver(detailsInstallReceiver);
            appActionDetails = null;
            disposable.clear();
        } catch (Exception ignored) {
        }
    }

    private void fetchApp() {
        disposable.add(Observable.fromCallable(() -> new FetchAppsTask(context)
                .getFullAppByPackageName(packageName))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((mApp) -> {
                    if (mApp != null) {
                        draw(mApp);
                    }
                }, err -> {
                    Log.e(err.getMessage());
                }));
    }

    private void draw(App mApp) {
        app = mApp;
        drawButtons();
        new AppInfoDetails(this, app).draw();
        new AppSubInfoDetails(this, app).draw();
        new AppLinkDetails(this, app).draw();
        new AppScreenshotsDetails(this, app).draw();
    }

    public void drawButtons() {
        appActionDetails = new AppActionDetails(this, app);
        AppPackages appPackages = new AppPackages(this, app);
        appActionDetails.draw();
        appPackages.draw();
    }

    private void notifyAction(String message) {
        Snackbar snackbar = Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_LONG);
        snackbar.setDuration(BaseTransientBottomBar.LENGTH_SHORT);
        snackbar.show();
    }
}
