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

package com.aurora.adroid.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aurora.adroid.R;
import com.aurora.adroid.adapter.ClusterAppsAdapter;
import com.aurora.adroid.database.AppDatabase;
import com.aurora.adroid.task.FetchAppsTask;
import com.aurora.adroid.util.Log;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class ClusterView extends RelativeLayout {

    @BindView(R.id.title)
    TextView txtTitle;
    @BindView(R.id.recycler)
    RecyclerView recyclerView;


    private CompositeDisposable disposable = new CompositeDisposable();
    private ClusterAppsAdapter clusterAppsAdapter;
    private String categoryName;

    public ClusterView(Context context) {
        super(context);
        init(context);
    }

    public ClusterView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ClusterView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public ClusterView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    private void init(Context context) {
        View view = inflate(context, R.layout.view_cluster_apps, this);
        ButterKnife.bind(this, view);
    }

    public void build() {
        txtTitle.setText("Related Apps");
        setupRecycler(getContext());
        fetchCategoryApps();
    }

    public void fetchCategoryApps() {
        disposable.add(Observable.fromCallable(() -> new FetchAppsTask(getContext())
                .getAppsByCategory(getCategoryName()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((appList) -> {
                    if (!appList.isEmpty())
                        clusterAppsAdapter.addData(appList);
                }, err -> {
                    Log.e(err.getMessage());
                }));
    }

    private void setupRecycler(Context context) {
        clusterAppsAdapter = new ClusterAppsAdapter(context);
        recyclerView.setLayoutManager(new LinearLayoutManager(context, RecyclerView.HORIZONTAL, false));
        recyclerView.setLayoutAnimation(AnimationUtils.loadLayoutAnimation(getContext(), R.anim.anim_slideright));
        recyclerView.setAdapter(clusterAppsAdapter);
    }
}
