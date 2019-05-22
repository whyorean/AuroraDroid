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

import android.text.TextUtils;
import android.widget.LinearLayout;

import com.aurora.adroid.R;
import com.aurora.adroid.fragment.DetailsFragment;
import com.aurora.adroid.model.App;
import com.aurora.adroid.sheet.MoreInfoSheet;
import com.aurora.adroid.util.Util;
import com.aurora.adroid.util.ViewUtil;
import com.aurora.adroid.view.ClusterView;
import com.google.android.material.chip.Chip;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;

public class AppSubInfoDetails extends AbstractDetails {


    @BindView(R.id.txt_updated)
    Chip chipUpdated;
    @BindView(R.id.txt_category)
    Chip chipCategory;
    @BindView(R.id.txt_size)
    Chip chipSize;
    @BindView(R.id.txt_licence)
    Chip chipLicense;
    @BindView(R.id.chip_description)
    Chip chipDescription;
    @BindView(R.id.txt_repo)
    Chip chipRepo;
    @BindView(R.id.cluster_container)
    LinearLayout layoutCluster;

    public AppSubInfoDetails(DetailsFragment fragment, App app) {
        super(fragment, app);
    }

    @Override
    public void draw() {
        final DateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        chipUpdated.setText(dateFormat.format(new Date(app.getLastUpdated())));
        chipLicense.setText(TextUtils.isEmpty(app.getLicense()) ? "unknown" : app.getLicense());
        chipSize.setText(Util.humanReadableByteValue(app.getAppPackage().getSize(), true));
        chipRepo.setText(TextUtils.isEmpty(app.getRepoName()) ? "unknown" : app.getRepoName());

        if (app.getCategories()!= null) {
            chipCategory.setText(app.getCategories().get(0));
            setupCluster(app.getCategories().get(0));
        } else
            ViewUtil.hideWithAnimation(chipCategory);

        chipDescription.setOnClickListener(v -> {
            MoreInfoSheet moreInfoSheet = new MoreInfoSheet();
            moreInfoSheet.setApp(app);
            moreInfoSheet.show(fragment.getChildFragmentManager(), "DESCRIPTION");
        });
    }

    private void setupCluster(String category) {
        ClusterView clusterView = new ClusterView(context);
        clusterView.setCategoryName(category);
        clusterView.build();
        layoutCluster.addView(clusterView);
    }
}
