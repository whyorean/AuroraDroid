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

package com.aurora.adroid.fragment.details;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aurora.adroid.R;
import com.aurora.adroid.adapter.ClusterAppsAdapter;
import com.aurora.adroid.fragment.DetailsFragment;
import com.aurora.adroid.model.App;
import com.aurora.adroid.model.Package;
import com.aurora.adroid.sheet.MoreInfoSheet;
import com.aurora.adroid.task.FetchAppsTask;
import com.aurora.adroid.util.Log;
import com.aurora.adroid.util.PackageUtil;
import com.aurora.adroid.util.Util;
import com.aurora.adroid.util.ViewUtil;
import com.google.android.material.chip.Chip;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class AppSubInfoDetails extends AbstractDetails {


    @BindView(R.id.txt_updated)
    Chip chipUpdated;
    @BindView(R.id.txt_category)
    Chip chipCategory;
    @BindView(R.id.txt_size)
    Chip chipSize;
    @BindView(R.id.txt_arch)
    Chip chipArch;
    @BindView(R.id.txt_sdk)
    Chip chipSdk;
    @BindView(R.id.txt_licence)
    Chip chipLicense;
    @BindView(R.id.txt_repo)
    Chip chipRepo;
    @BindView(R.id.img_more)
    ImageButton imgMore;
    @BindView(R.id.layout_developer)
    LinearLayout layoutDeveloper;
    @BindView(R.id.layout_similar)
    LinearLayout layoutSimilar;
    @BindView(R.id.recycler_developer)
    RecyclerView recyclerDeveloper;
    @BindView(R.id.recycler_similar)
    RecyclerView recyclerSimilar;

    private CompositeDisposable disposable = new CompositeDisposable();
    private ClusterAppsAdapter adapterDeveloper;
    private ClusterAppsAdapter adapterSimilar;

    public AppSubInfoDetails(DetailsFragment fragment, App app) {
        super(fragment, app);
    }

    @Override
    public void draw() {
        final DateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        final Package pkg = app.getAppPackage();
        chipUpdated.setText(dateFormat.format(new Date(app.getLastUpdated())));
        chipLicense.setText(TextUtils.isEmpty(app.getLicense()) ? "unknown" : app.getLicense());
        chipSize.setText(Util.humanReadableByteValue(pkg.getSize(), true));
        chipArch.setText(PackageUtil.getPackageArchName(pkg));
        chipSdk.setText(String.format(Locale.getDefault(), "Min SDK %s", pkg.getMinSdkVersion()));
        chipRepo.setText(TextUtils.isEmpty(app.getRepoName()) ? "unknown" : app.getRepoName());

        if (app.getCategories() != null && !app.getCategories().isEmpty()) {
            chipCategory.setText(app.getCategories().get(0));
            layoutSimilar.setVisibility(View.VISIBLE);
            setupSimilarRecycler(context);
            fetchSimilarApps(app.getCategories().get(0));
        } else
            ViewUtil.hideWithAnimation(chipCategory);

        imgMore.setOnClickListener(v -> {
            MoreInfoSheet moreInfoSheet = new MoreInfoSheet();
            moreInfoSheet.setApp(app);
            moreInfoSheet.show(fragment.getChildFragmentManager(), "DESCRIPTION");
        });

        if (app.getAuthorName() != null
                && !app.getAuthorName().isEmpty()
                && !app.getAuthorName().equalsIgnoreCase("unknown")) {
            layoutDeveloper.setVisibility(View.VISIBLE);
            setupAuthorRecycler(context);
            fetchAuthorApps();
        }
    }

    private void fetchAuthorApps() {
        disposable.add(Observable.fromCallable(() -> new FetchAppsTask(context)
                .getAppsByDeveloperName(app.getAuthorName(), app.getPackageName()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((appList) -> {
                    if (appList.isEmpty())
                        layoutDeveloper.setVisibility(View.GONE);
                    else
                        adapterDeveloper.addData(appList);

                }, err -> {
                    Log.e(err.getMessage());
                }));
    }

    private void fetchSimilarApps(String category) {
        disposable.add(Observable.fromCallable(() -> new FetchAppsTask(context)
                .getAppsByCategory(category))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((appList) -> {
                    if (appList.isEmpty())
                        layoutSimilar.setVisibility(View.GONE);
                    else
                        adapterSimilar.addData(appList);
                }, err -> {
                    Log.e(err.getMessage());
                }));
    }

    private void setupAuthorRecycler(Context context) {
        adapterDeveloper = new ClusterAppsAdapter(context);
        recyclerDeveloper.setLayoutManager(new LinearLayoutManager(context, RecyclerView.HORIZONTAL, false));
        recyclerDeveloper.setAdapter(adapterDeveloper);
    }

    private void setupSimilarRecycler(Context context) {
        adapterSimilar = new ClusterAppsAdapter(context);
        recyclerSimilar.setLayoutManager(new LinearLayoutManager(context, RecyclerView.HORIZONTAL, false));
        recyclerSimilar.setAdapter(adapterSimilar);
    }
}
