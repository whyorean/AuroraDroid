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

package com.aurora.adroid.ui.fragment.details;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;
import androidx.palette.graphics.Palette;

import com.aurora.adroid.GlideApp;
import com.aurora.adroid.R;
import com.aurora.adroid.model.App;
import com.aurora.adroid.ui.activity.DetailsActivity;
import com.aurora.adroid.util.ColorUtil;
import com.aurora.adroid.util.DatabaseUtil;
import com.aurora.adroid.util.PackageUtil;
import com.aurora.adroid.util.TextUtil;
import com.aurora.adroid.util.ThemeUtil;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
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
                    .listener(new RequestListener<Bitmap>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                            getPalette(resource);
                            return false;
                        }
                    })
                    .into(imgIcon);

        txtName.setText(app.getName());
        setText(txtPackageName, app.getPackageName());
        setText(txtDevName, app.getAuthorName());

        String summary;
        if (app.getLocalized() != null
                && app.getLocalized().getEnUS() != null
                && app.getLocalized().getEnUS().getSummary() != null) {
            summary = TextUtil.emptyIfNull(app.getLocalized().getEnUS().getSummary());
        } else
            summary = TextUtil.emptyIfNull(app.getSummary());

        if (!summary.isEmpty()) {
            setText(txtSummary, StringUtils.capitalize(summary.trim()));
        }

        if (PackageUtil.isInstalled(context, app.getPackageName()))
            drawVersion();
        else
            txtVersion.setText(StringUtils.joinWith(".",
                    app.getAppPackage().getVersionName(),
                    app.getAppPackage().getVersionCode()));
    }

    private void drawVersion() {
        try {
            final PackageInfo info = context.getPackageManager().getPackageInfo(app.getPackageName(), 0);
            final String currentVersion = StringUtils.joinWith(".",
                    info.versionName,
                    info.versionCode);
            final String updateVersion = StringUtils.joinWith(".",
                    app.getAppPackage().getVersionName(),
                    app.getAppPackage().getVersionCode());

            if (app.getAppPackage().getVersionCode() > info.versionCode)
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

    private void getPalette(Bitmap bitmap) {
        Palette.from(bitmap).generate(palette -> {
            if (palette != null)
                paintEmAll(palette);
        });
    }

    private void paintEmAll(Palette palette) {
        Palette.Swatch swatch = palette.getDarkVibrantSwatch();
        int colorPrimary = Color.GRAY;
        int colorPrimaryText = Color.BLACK;

        //Make sure we get a fallback swatch if DarkVibrantSwatch is not available
        if (swatch == null)
            swatch = palette.getVibrantSwatch();

        //Make sure we get another fallback swatch if VibrantSwatch is not available
        if (swatch == null)
            swatch = palette.getDominantSwatch();

        if (swatch != null) {
            colorPrimary = swatch.getRgb();
            colorPrimaryText = ColorUtil.manipulateColor(colorPrimary, 0.3f);
        }

        if (ColorUtil.isColorLight(colorPrimary))
            btnPositive.setTextColor(Color.BLACK);
        else
            btnPositive.setTextColor(Color.WHITE);

        btnPositive.setBackgroundColor(colorPrimary);
        btnPositive.setStrokeColor(ColorStateList.valueOf(colorPrimary));

        if (ThemeUtil.isLightTheme(context)) {
            btnNegative.setTextColor(colorPrimaryText);
            txtDevName.setTextColor(colorPrimaryText);
            txtSummary.setTextColor(colorPrimaryText);
            txtSummary.setBackgroundTintList(ColorStateList.valueOf(ColorUtils.setAlphaComponent(colorPrimary, 60)));
        }
    }
}
