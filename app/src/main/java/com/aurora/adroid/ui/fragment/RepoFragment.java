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

package com.aurora.adroid.ui.fragment;

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

import com.aurora.adroid.AuroraApplication;
import com.aurora.adroid.R;
import com.aurora.adroid.event.LogEvent;
import com.aurora.adroid.service.SyncService;
import com.aurora.adroid.ui.activity.AuroraActivity;
import com.aurora.adroid.ui.activity.ContainerActivity;
import com.aurora.adroid.ui.activity.IntroActivity;
import com.aurora.adroid.ui.sheet.RepoAddSheet;
import com.aurora.adroid.ui.sheet.RepoListSheet;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class RepoFragment extends Fragment {

    @BindView(R.id.coordinator)
    CoordinatorLayout coordinatorLayout;
    @BindView(R.id.layout_repo_list)
    RelativeLayout repoLayout;
    @BindView(R.id.txtLog)
    TextView txtLog;
    @BindView(R.id.btn_sync)
    MaterialButton btnSync;

    private CompositeDisposable disposable = new CompositeDisposable();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        disposable.add(AuroraApplication.getRxBus()
                .getBus()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(event -> {

                    switch (event.getType()) {
                        case SYNC_EMPTY:
                            notifyAction(getString(R.string.toast_no_repo_selected));
                            break;
                        case SYNC_COMPLETED:
                            syncCompleted();
                            break;
                        case SYNC_FAILED:
                            syncFailed();
                            break;
                    }

                    if (event instanceof LogEvent) {
                        String msg = ((LogEvent) event).getMessage();
                        txtLog.append("\n" + msg);
                    }
                }));
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
    public void onDestroy() {
        try {
            disposable.clear();
        } catch (Exception ignored) {
        }
        super.onDestroy();
    }

    @OnClick(R.id.layout_repo_add)
    public void addRepo() {
        final FragmentManager fragmentManager = getChildFragmentManager();
        if (fragmentManager.findFragmentByTag(RepoAddSheet.TAG) == null) {
            final RepoAddSheet sheet = new RepoAddSheet();
            sheet.setCancelable(false);
            sheet.show(fragmentManager, RepoAddSheet.TAG);
        }
    }

    @OnClick(R.id.layout_repo_list)
    public void showAllRepos() {
        final FragmentManager fragmentManager = getChildFragmentManager();
        if (fragmentManager.findFragmentByTag(RepoListSheet.TAG) == null) {
            final RepoListSheet sheet = new RepoListSheet();
            sheet.show(fragmentManager, RepoListSheet.TAG);
            init();
        }
    }

    private void init() {
        btnSync.setText(getString(R.string.action_sync));
        btnSync.setOnClickListener(v -> startRepoSyncService());
        txtLog.setMovementMethod(new ScrollingMovementMethod());
        if (SyncService.isServiceRunning())
            blockSync();
    }

    private void startRepoSyncService() {
        final Intent intent = new Intent(requireActivity(), SyncService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            requireActivity().startForegroundService(intent);
        } else {
            requireActivity().startService(intent);
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
                getActivity().startActivity(new Intent(requireActivity(), AuroraActivity.class));
                getActivity().finish();
            });
        else if (getActivity() instanceof ContainerActivity)
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

    private void notifyAction(String message) {
        Snackbar snackbar = Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_LONG);
        snackbar.setDuration(BaseTransientBottomBar.LENGTH_SHORT);
        snackbar.show();
    }
}
