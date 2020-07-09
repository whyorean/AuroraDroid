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

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.aurora.adroid.GlideApp;
import com.aurora.adroid.R;
import com.aurora.adroid.model.App;
import com.aurora.adroid.ui.activity.DetailsActivity;
import com.aurora.adroid.util.DatabaseUtil;
import com.aurora.adroid.util.LocalizationUtil;
import com.aurora.adroid.util.PackageUtil;
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions;
import com.google.android.material.button.MaterialButton;

import org.apache.commons.lang3.StringUtils;

import butterknife.BindView;

public class AppInfoDetails extends AbstractDetails {

    @BindView(R.id.img)
    ImageView imgIcon;
    @BindView(R.id.txt_name)
    TextView txtName;
    @BindView(R.id.txt_package_name)
    TextView txtPackageName;
    @BindView(R.id.txt_dev_name)
    TextView txtDevName;
    @BindView(R.id.line2)
    TextView txtVersion;
    @BindView(R.id.txt_summary)
    TextView txtSummary;
    @BindView(R.id.btn_positive)
    MaterialButton btnPositive;
    @BindView(R.id.btn_negative)
    MaterialButton btnNegative;

    public AppInfoDetails(DetailsActivity activity, App app) {
        super(activity, app);
    }

    @Override
    public void draw() {
        if (app.getIcon() == null)
            imgIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_placeholder));
        else
            GlideApp.with(context)
                    .asBitmap()
                    .load(DatabaseUtil.getImageUrl(app))
                    .transition(new BitmapTransitionOptions().crossFade())
                    .into(imgIcon);

        txtName.setText(app.getName());
        setText(txtPackageName, app.getPackageName());
        setText(txtDevName, app.getAuthorName());

        String summary = LocalizationUtil.getLocalizedSummary(context, app);

        if (!summary.isEmpty()) {
            setText(txtSummary, StringUtils.capitalize(summary.trim()));
        }

        if (app.getPkg() != null) {
            if (PackageUtil.isInstalled(context, app.getPackageName()))
                drawVersion();
            else {
                txtVersion.setText(StringUtils.joinWith(".",
                        app.getPkg().getVersionName(),
                        app.getPkg().getVersionCode()));
            }
        }
    }

    private void drawVersion() {
        try {
            final PackageInfo info = context.getPackageManager().getPackageInfo(app.getPackageName(), 0);
            final String currentVersion = StringUtils.joinWith(".",
                    info.versionName,
                    info.versionCode);
            final String updateVersion = StringUtils.joinWith(".",
                    app.getPkg().getVersionName(),
                    app.getPkg().getVersionCode());

            if (app.getPkg().getVersionCode() > info.versionCode)
                txtVersion.setText(new StringBuilder()
                        .append(currentVersion)
                        .append(" >> ")
                        .append(updateVersion));
            else
                txtVersion.setText(currentVersion);
        } catch (PackageManager.NameNotFoundException e) {
            // We've checked for that already
        }
    }
}
