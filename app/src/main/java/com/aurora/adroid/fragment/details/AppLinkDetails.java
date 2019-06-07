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

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;

import com.aurora.adroid.R;
import com.aurora.adroid.fragment.DetailsFragment;
import com.aurora.adroid.model.App;
import com.aurora.adroid.sheet.PermissionBottomSheet;
import com.aurora.adroid.util.Log;
import com.aurora.adroid.util.PackageUtil;
import com.aurora.adroid.view.LinkView;

import butterknife.BindView;

public class AppLinkDetails extends AbstractDetails {


    @BindView(R.id.layout_link_perm)
    LinearLayout linkLayout;


    public AppLinkDetails(DetailsFragment fragment, App app) {
        super(fragment, app);
    }

    @Override
    public void draw() {

        LinkView permLinkView = new LinkView(context);
        permLinkView.setLinkText("Permission");
        permLinkView.setLinkImageId(R.drawable.ic_permission_link);
        permLinkView.setColor(R.color.colorGold);
        permLinkView.setOnClickListener(v -> {
            PermissionBottomSheet profileFragment = new PermissionBottomSheet();
            profileFragment.setApp(app);
            profileFragment.show(fragment.getChildFragmentManager(), "PERMISSION");
        });
        permLinkView.build();

        LinkView sourceLinkView = new LinkView(context);
        sourceLinkView.setVisibility(TextUtils.isEmpty(app.getSourceCode()) ? View.GONE : View.VISIBLE);
        sourceLinkView.setLinkText("Source");
        sourceLinkView.setLinkImageId(R.drawable.ic_source_link);
        sourceLinkView.setColor(R.color.colorCyan);
        sourceLinkView.setOnClickListener(v -> {
            openWebView(app.getSourceCode());
        });
        sourceLinkView.build();

        LinkView websiteLinkView = new LinkView(context);
        websiteLinkView.setVisibility(TextUtils.isEmpty(app.getWebSite()) ? View.GONE : View.VISIBLE);
        websiteLinkView.setLinkText("Website");
        websiteLinkView.setLinkImageId(R.drawable.ic_web_link);
        websiteLinkView.setColor(R.color.colorPurple);
        websiteLinkView.setOnClickListener(v -> {
            openWebView(app.getWebSite());
        });
        websiteLinkView.build();

        LinkView donationLinkView = new LinkView(context);
        donationLinkView.setVisibility(TextUtils.isEmpty(app.getDonate()) ? View.GONE : View.VISIBLE);
        donationLinkView.setLinkText("Donation");
        donationLinkView.setLinkImageId(R.drawable.ic_donation_link);
        donationLinkView.setColor(R.color.colorOrange);
        donationLinkView.setOnClickListener(v -> {
            openWebView(app.getDonate());
        });
        donationLinkView.build();

        LinkView settingsLinkView = new LinkView(context);
        settingsLinkView.setVisibility(PackageUtil.isInstalled(context, app.getPackageName()) ? View.VISIBLE : View.GONE);
        settingsLinkView.setLinkText("Settings");
        settingsLinkView.setLinkImageId(R.drawable.ic_settings_link);
        settingsLinkView.setColor(R.color.colorGreen);
        settingsLinkView.setOnClickListener(v -> {
            try {
                context.startActivity(new Intent("android.settings.APPLICATION_DETAILS_SETTINGS",
                        Uri.parse("package:" + app.getPackageName())));
            } catch (ActivityNotFoundException e) {
                Log.e("Could not find system app activity");
            }
        });

        settingsLinkView.build();
        linkLayout.removeAllViews();
        linkLayout.addView(permLinkView);
        linkLayout.addView(sourceLinkView);
        linkLayout.addView(websiteLinkView);
        linkLayout.addView(donationLinkView);
        linkLayout.addView(settingsLinkView);
    }

    private void openWebView(String URL) {
        try {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(URL)));
        } catch (Exception e) {
            Log.e("No WebView found !");
        }
    }
}
