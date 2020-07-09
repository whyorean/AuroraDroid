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

package com.aurora.adroid.ui.fragment.details;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aurora.adroid.Constants;
import com.aurora.adroid.R;
import com.aurora.adroid.model.App;
import com.aurora.adroid.model.Package;
import com.aurora.adroid.model.items.cluster.GenericClusterItem;
import com.aurora.adroid.ui.activity.DetailsActivity;
import com.aurora.adroid.ui.sheet.MoreInfoSheet;
import com.aurora.adroid.util.PackageUtil;
import com.aurora.adroid.util.Util;
import com.aurora.adroid.util.ViewUtil;
import com.aurora.adroid.viewmodel.ClusterAppsViewModel;
import com.google.android.material.chip.Chip;
import com.mikepenz.fastadapter.adapters.FastItemAdapter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import io.reactivex.Observable;

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

    public AppSubInfoDetails(DetailsActivity activity, App app) {
        super(activity, app);
    }

    @Override
    public void draw() {
        final ClusterAppsViewModel clusterAppsViewModel = new ViewModelProvider(activity).get(ClusterAppsViewModel.class);
        final DateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        final Package pkg = app.getPkg();

        chipUpdated.setText(dateFormat.format(new Date(app.getLastUpdated())));
        chipLicense.setText(TextUtils.isEmpty(app.getLicense()) ? "unknown" : app.getLicense());
        chipSize.setText(Util.humanReadableByteValue(pkg.getSize(), true));
        chipArch.setText(PackageUtil.getPackageArchName(pkg));
        chipSdk.setText(String.format(Locale.getDefault(), "Min SDK %s", pkg.getMinSdkVersion()));
        chipRepo.setText(TextUtils.isEmpty(app.getRepoName()) ? "unknown" : app.getRepoName());

        if (app.getCategories() != null && !app.getCategories().isEmpty()) {
            chipCategory.setText(app.getCategories().get(0));
            layoutSimilar.setVisibility(View.VISIBLE);
            clusterAppsViewModel.getCategoryAppsLiveData(app.getCategories().get(0))
                    .observe(activity, this::setupSimilarRecycler);
        } else
            ViewUtil.hideWithAnimation(chipCategory);

        imgMore.setOnClickListener(v -> {
            MoreInfoSheet moreInfoSheet = new MoreInfoSheet();
            moreInfoSheet.setApp(app);
            moreInfoSheet.show(activity.getSupportFragmentManager(), "DESCRIPTION");
        });

        if (app.getAuthorName() != null
                && !app.getAuthorName().isEmpty()
                && !app.getAuthorName().equalsIgnoreCase("unknown")) {
            layoutDeveloper.setVisibility(View.VISIBLE);
            clusterAppsViewModel.getAuthorAppsLiveData(app.getAuthorName())
                    .observe(activity, this::setupAuthorRecycler);
        }
    }

    private void setupAuthorRecycler(List<App> appList) {
        FastItemAdapter<GenericClusterItem> fastItemAdapter = new FastItemAdapter<>();
        fastItemAdapter.setOnClickListener((view, iAdapter, item, position) -> {
            App app = item.getApp();
            Intent intent = new Intent(context, DetailsActivity.class);
            intent.putExtra(Constants.INTENT_PACKAGE_NAME, app.getPackageName());
            intent.putExtra(Constants.STRING_REPO, app.getRepoName());
            context.startActivity(intent);
            return false;
        });

        recyclerDeveloper.setLayoutManager(new LinearLayoutManager(context, RecyclerView.HORIZONTAL, false));
        recyclerDeveloper.setAdapter(fastItemAdapter);

        Observable.fromIterable(appList)
                .map(GenericClusterItem::new)
                .toList()
                .doOnSuccess(fastItemAdapter::add)
                .subscribe();
    }

    private void setupSimilarRecycler(List<App> appList) {
        FastItemAdapter<GenericClusterItem> fastItemAdapter = new FastItemAdapter<>();
        fastItemAdapter.setOnClickListener((view, iAdapter, item, position) -> {
            App app = item.getApp();
            Intent intent = new Intent(context, DetailsActivity.class);
            intent.putExtra(Constants.INTENT_PACKAGE_NAME, app.getPackageName());
            intent.putExtra(Constants.STRING_REPO, app.getRepoName());
            context.startActivity(intent);
            return false;
        });

        recyclerSimilar.setLayoutManager(new LinearLayoutManager(context, RecyclerView.HORIZONTAL, false));
        recyclerSimilar.setAdapter(fastItemAdapter);

        Observable.fromIterable(appList)
                .map(GenericClusterItem::new)
                .toList()
                .doOnSuccess(fastItemAdapter::add)
                .subscribe();
    }
}
