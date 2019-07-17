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
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aurora.adroid.ErrorType;
import com.aurora.adroid.R;
import com.aurora.adroid.activity.AuroraActivity;
import com.aurora.adroid.adapter.UpdatableAppsAdapter;
import com.aurora.adroid.download.DownloadManager;
import com.aurora.adroid.model.App;
import com.aurora.adroid.task.InstalledAppTask;
import com.aurora.adroid.task.LiveUpdate;
import com.aurora.adroid.util.Log;
import com.aurora.adroid.util.ViewUtil;
import com.aurora.adroid.view.CustomSwipeToRefresh;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.tonyodev.fetch2.Fetch;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class UpdatesFragment extends BaseFragment {

    private static final int UPDATE_GROUP_ID = 1338;

    @BindView(R.id.swipe_layout)
    CustomSwipeToRefresh customSwipeToRefresh;
    @BindView(R.id.recycler)
    RecyclerView recyclerView;
    @BindView(R.id.btn_update_all)
    Button btnUpdateAll;
    @BindView(R.id.txt_update_all)
    TextView txtUpdateAll;

    private Context context;
    private Fetch fetch;
    private boolean onGoingUpdate = false;
    private BottomNavigationView bottomNavigationView;
    private UpdatableAppsAdapter updatableAppsAdapter;
    private List<App> updatableAppList = new ArrayList<>();
    private CompositeDisposable disposable = new CompositeDisposable();

    @Override
    protected View.OnClickListener errRetry() {
        return v -> {
            fetchData();
            ((Button) v).setText(getString(R.string.action_recheck_ing));
            ((Button) v).setEnabled(false);
        };
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
        View view = inflater.inflate(R.layout.fragment_updates, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fetch = DownloadManager.getFetchInstance(context);
        updatableAppsAdapter = new UpdatableAppsAdapter(context);
        setErrorView(ErrorType.UNKNOWN);
        setupRecycler();
        customSwipeToRefresh.setOnRefreshListener(() -> fetchData());
        if (getActivity() instanceof AuroraActivity)
            bottomNavigationView = ((AuroraActivity) getActivity()).getBottomNavigationView();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (updatableAppsAdapter != null && updatableAppsAdapter.isDataEmpty())
            fetchData();
    }

    @Override
    public void onPause() {
        super.onPause();
        customSwipeToRefresh.setRefreshing(false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disposable.clear();
    }

    private void fetchData() {
        disposable.add(Observable.fromCallable(() -> new InstalledAppTask(context)
                .getUpdatableApps())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(subscription -> customSwipeToRefresh.setRefreshing(true))
                .doOnComplete(() -> customSwipeToRefresh.setRefreshing(false))
                .subscribe((appList) -> {
                    if (appList.isEmpty()) {
                        setErrorView(ErrorType.NO_UPDATES);
                        switchViews(true);
                    } else {
                        switchViews(false);
                        updatableAppList = appList;
                        updatableAppsAdapter.addData(appList);
                        updateCounter();
                        setupUpdateAll();
                    }
                }, err -> {
                    Log.e(err.getMessage());
                    err.printStackTrace();
                }));
    }

    private void setupRecycler() {
        recyclerView.setAdapter(updatableAppsAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL, false));
        recyclerView.setLayoutAnimation(AnimationUtils.loadLayoutAnimation(context, R.anim.anim_falldown));
        recyclerView.setOnFlingListener(new RecyclerView.OnFlingListener() {
            @Override
            public boolean onFling(int velocityX, int velocityY) {
                if (velocityY < 0) {
                    if (bottomNavigationView != null)
                        ViewUtil.showBottomNav(bottomNavigationView, true);
                } else if (velocityY > 0) {
                    if (bottomNavigationView != null)
                        ViewUtil.hideBottomNav(bottomNavigationView, true);
                }
                return false;
            }
        });
    }

    private void setupUpdateAll() {
        if (updatableAppList.isEmpty()) {
            ViewUtil.hideWithAnimation(btnUpdateAll);
            txtUpdateAll.setText(context.getString(R.string.list_empty_updates));
        } else if (onGoingUpdate) {
            btnUpdateAll.setOnClickListener(cancelAllListener());
        } else {
            btnUpdateAll.setOnClickListener(updateAllListener());
        }
    }

    private void updateCounter() {
        txtUpdateAll.setText(new StringBuilder()
                .append(updatableAppList.size())
                .append(StringUtils.SPACE)
                .append(context.getString(R.string.list_update_all_txt)));
    }

    private View.OnClickListener updateAllListener() {
        btnUpdateAll.setText(getString(R.string.list_update_all));
        btnUpdateAll.setEnabled(true);
        return v -> {
            updateAllApps();
            onGoingUpdate = true;
            btnUpdateAll.setText(getString(R.string.list_updating));
            btnUpdateAll.setEnabled(false);
        };
    }

    private View.OnClickListener cancelAllListener() {
        btnUpdateAll.setText(getString(R.string.action_cancel));
        btnUpdateAll.setEnabled(true);
        return v -> {
            fetch.deleteGroup(UPDATE_GROUP_ID);
            setupUpdateAll();
        };
    }

    private void updateAllApps() {
        disposable.add(Observable.fromIterable(updatableAppList)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(app -> new LiveUpdate(context, app).enqueueUpdate())
                .doOnError(throwable -> {
                    Log.e(throwable.getMessage());
                })
                .subscribe());
    }
}
