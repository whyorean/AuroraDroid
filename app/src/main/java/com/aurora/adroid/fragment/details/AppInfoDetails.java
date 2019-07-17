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

import android.widget.ImageView;
import android.widget.TextView;

import com.aurora.adroid.GlideApp;
import com.aurora.adroid.R;
import com.aurora.adroid.fragment.DetailsFragment;
import com.aurora.adroid.model.App;
import com.aurora.adroid.util.DatabaseUtil;
import com.aurora.adroid.util.TextUtil;

import butterknife.BindView;

public class AppInfoDetails extends AbstractDetails {

    @BindView(R.id.img_icon)
    ImageView imgIcon;
    @BindView(R.id.txt_name)
    TextView txtName;
    @BindView(R.id.txt_package_name)
    TextView txtPackageName;
    @BindView(R.id.txt_dev_name)
    TextView txtDevName;
    @BindView(R.id.txt_version)
    TextView txtVersion;

    @BindView(R.id.txt_summary)
    TextView txtSummary;

    public AppInfoDetails(DetailsFragment fragment, App app) {
        super(fragment, app);
    }

    @Override
    public void draw() {
        GlideApp
                .with(context)
                .asBitmap()
                .load(DatabaseUtil.getImageUrl(app))
                .placeholder(R.drawable.ic_placeholder)
                .into(imgIcon);
        txtName.setText(app.getName());
        txtVersion.setText(new StringBuilder()
                .append(app.getAppPackage().getVersionName())
                .append(".")
                .append(app.getAppPackage().getVersionCode()));
        setText(txtPackageName, app.getPackageName());
        setText(txtDevName, app.getAuthorName());
        if (app.getLocalized() != null && app.getLocalized().getEnUS() != null && !TextUtil.isEmpty(app.getLocalized().getEnUS().getSummary()))
            setText(txtSummary, app.getLocalized().getEnUS().getSummary());
        else
            setText(txtSummary, TextUtil.emptyIfNull(app.getSummary()));
    }
}
