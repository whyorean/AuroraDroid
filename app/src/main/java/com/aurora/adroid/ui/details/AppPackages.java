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

package com.aurora.adroid.ui.details;

import android.view.View;
import android.widget.RelativeLayout;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aurora.adroid.R;
import com.aurora.adroid.model.App;
import com.aurora.adroid.model.items.PackageItem;
import com.aurora.adroid.ui.view.HeaderLayout;
import com.aurora.adroid.util.Log;
import com.aurora.adroid.util.ViewUtil;
import com.mikepenz.fastadapter.adapters.FastItemAdapter;

import butterknife.BindView;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;

public class AppPackages extends AbstractDetails {

    @BindView(R.id.layout_other_version)
    HeaderLayout headerLayout;
    @BindView(R.id.layout_version)
    RelativeLayout layoutVersion;

    @BindView(R.id.package_recycler)
    RecyclerView recyclerView;

    private FastItemAdapter<PackageItem> fastItemAdapter;
    private CompositeDisposable disposable = new CompositeDisposable();

    public AppPackages(DetailsActivity activity, App app) {
        super(activity, app);
    }

    @Override
    public void draw() {

        fastItemAdapter = new FastItemAdapter<>();
        recyclerView.setAdapter(fastItemAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL, false));
        headerLayout.setOnClickListener(v -> {
            if (layoutVersion.getVisibility() == View.GONE) {
                ViewUtil.rotateView(headerLayout.getImgAction(), false);
                ViewUtil.showWithAnimation(layoutVersion);
            } else {
                ViewUtil.rotateView(headerLayout.getImgAction(), true);
                ViewUtil.hideWithAnimation(layoutVersion);
            }
        });

        disposable.add(Observable.fromIterable(app.getAppPackage().getPackageList())
                .map(pkg -> new PackageItem(pkg, app))
                .toList()
                .subscribe(packageItems -> {
                    fastItemAdapter.add(packageItems);
                }, throwable -> {
                    Log.e(throwable.getMessage());
                }));
    }
}
