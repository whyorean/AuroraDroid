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
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.aurora.adroid.AuroraApplication;
import com.aurora.adroid.R;
import com.aurora.adroid.activity.AuroraActivity;
import com.aurora.adroid.activity.IntroActivity;
import com.aurora.adroid.activity.SettingsActivity;
import com.aurora.adroid.event.Event;
import com.aurora.adroid.event.Events;
import com.aurora.adroid.event.LogEvent;
import com.aurora.adroid.service.RepoSyncService;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class RepoFragment extends Fragment {

    @BindView(R.id.coordinator)
    CoordinatorLayout coordinatorLayout;
    @BindView(R.id.repo_list)
    RelativeLayout repoLayout;
    @BindView(R.id.txtLog)
    TextView txtLog;
    @BindView(R.id.btn_sync)
    MaterialButton btnSync;

    private Context context;
    private CompositeDisposable disposable = new CompositeDisposable();

    @Override
    public void onStart() {
        super.onStart();
        disposable.add(AuroraApplication.getRxBus()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(event -> {
                    if (event instanceof Event) {
                        Events eventEnum = ((Event) event).getEvent();
                        switch (eventEnum) {
                            case SYNC_EMPTY:
                                notifyAction(getString(R.string.toast_no_repo_selected));
                                break;
                            case SYNC_COMPLETED:
                                syncCompleted();
                                break;
                            case SYNC_FAILED:
                                syncFailed();
                                break;
                            case SYNC_PROGRESS:

                                break;
                        }
                    }

                    if (event instanceof LogEvent) {
                        String msg = ((LogEvent) event).getMessage();
                        txtLog.append("\n" + msg);
                    }
                }));
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_repo, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        init();
    }

    @Override
    public void onStop() {
        try {
            disposable.clear();
            disposable.dispose();
        } catch (Exception ignored) {
        }
        super.onStop();
    }

    private void init() {
        repoLayout.setOnClickListener(v -> openRepoListFragment());
        btnSync.setOnClickListener(v -> startRepoSyncService());
        txtLog.setMovementMethod(new ScrollingMovementMethod());
        if (RepoSyncService.isServiceRunning())
            blockSync();
    }

    private void openRepoListFragment() {
        RepoListFragment fragment = new RepoListFragment();
        FragmentManager fragmentManager = getFragmentManager();
        if (fragmentManager != null)
            fragmentManager
                    .beginTransaction()
                    .add(R.id.coordinator, fragment)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .addToBackStack(null)
                    .commitAllowingStateLoss();
    }

    private void startRepoSyncService() {
        Intent intent = new Intent(context, RepoSyncService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
        blockSync();
    }

    private void blockSync() {
        btnSync.setEnabled(false);
        btnSync.setText(getString(R.string.sync_progress));
    }

    private void syncCompleted() {
        txtLog.append("\n" + getString(R.string.sync_completed_all));
        btnSync.setText(getString(R.string.action_finish));
        btnSync.setEnabled(true);

        if (getActivity() instanceof IntroActivity)
            btnSync.setOnClickListener(v -> {
                getActivity().startActivity(new Intent(context, AuroraActivity.class));
                getActivity().finish();
            });
        else if (getActivity() instanceof SettingsActivity)
            btnSync.setOnClickListener(v -> {
                getActivity().onBackPressed();
            });
    }

    private void syncFailed() {
        btnSync.setText(getString(R.string.action_sync));
        btnSync.setEnabled(true);
        txtLog.setText(getString(R.string.sys_log));
        btnSync.setOnClickListener(v -> startRepoSyncService());
    }

    private float getProgress(int current, int max) {
        float percent = ((float) current / (float) max) * 100.0f;
        return ((int) percent);
    }

    private void notifyAction(String message) {
        Snackbar snackbar = Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_LONG);
        snackbar.setDuration(BaseTransientBottomBar.LENGTH_SHORT);
        snackbar.show();
    }
}
