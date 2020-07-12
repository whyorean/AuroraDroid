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

package com.aurora.adroid.ui.sheet;

import android.content.pm.PackageManager;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.PermissionInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aurora.adroid.Constants;
import com.aurora.adroid.PermissionGroup;
import com.aurora.adroid.R;
import com.aurora.adroid.model.App;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PermissionSheet extends BaseBottomSheet {

    public static final String TAG = PermissionSheet.class.getName();

    @BindView(R.id.permissions_header)
    TextView viewHeader;
    @BindView(R.id.permissions_container)
    LinearLayout viewContainer;
    @BindView(R.id.permissions_container_widgets)
    LinearLayout container;
    @BindView(R.id.permissions_none)
    TextView permissions_none;

    private PackageManager packageManager;

    @NonNull
    @Override
    public View onCreateContentView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sheet_permissions, container, false);
        ButterKnife.bind(this, view);
        packageManager = requireContext().getPackageManager();
        return view;
    }

    @Override
    public void onContentViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final Bundle bundle = getArguments();
        if (bundle != null) {
            String rawApp = bundle.getString(Constants.STRING_EXTRA);
            if (rawApp != null) {
                App app = gson.fromJson(rawApp, App.class);
                addPermissionWidgets(app);
            }
        }
    }

    private void addPermissionWidgets(App app) {
        Map<String, PermissionGroup> permissionGroupWidgets = new HashMap<>();
        if (app.getPkg().getUsesPermission() == null || app.getPkg().getUsesPermission().isEmpty()) {
            permissions_none.setVisibility(permissionGroupWidgets.isEmpty() ? View.VISIBLE : View.GONE);
            return;
        }

        for (List<String> permissionList : app.getPkg().getUsesPermission()) {
            for (String permissionName : permissionList) {
                PermissionInfo permissionInfo = getPermissionInfo(permissionName);
                if (null == permissionInfo) {
                    continue;
                }
                PermissionGroup widget;
                PermissionGroupInfo permissionGroupInfo = getPermissionGroupInfo(permissionInfo);
                if (!permissionGroupWidgets.containsKey(permissionGroupInfo.name)) {
                    widget = new PermissionGroup(getContext());
                    widget.setPermissionGroupInfo(permissionGroupInfo);
                    widget.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT));
                    permissionGroupWidgets.put(permissionGroupInfo.name, widget);
                } else {
                    widget = permissionGroupWidgets.get(permissionGroupInfo.name);
                }
                if (widget != null) {
                    widget.addPermission(permissionInfo);
                }
            }
        }
        container.removeAllViews();
        List<String> permissionGroupLabels = new ArrayList<>(permissionGroupWidgets.keySet());
        Collections.sort(permissionGroupLabels);
        for (String permissionGroupLabel : permissionGroupLabels) {
            container.addView(permissionGroupWidgets.get(permissionGroupLabel));
        }
        permissions_none.setVisibility(permissionGroupWidgets.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private PermissionInfo getPermissionInfo(String permissionName) {
        try {
            return packageManager.getPermissionInfo(permissionName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    private PermissionGroupInfo getPermissionGroupInfo(PermissionInfo permissionInfo) {
        PermissionGroupInfo permissionGroupInfo;
        if (null == permissionInfo.group) {
            permissionGroupInfo = getFakePermissionGroupInfo(permissionInfo.packageName);
        } else {
            try {
                permissionGroupInfo = packageManager.getPermissionGroupInfo(permissionInfo.group, 0);
            } catch (PackageManager.NameNotFoundException e) {
                permissionGroupInfo = getFakePermissionGroupInfo(permissionInfo.packageName);
            }
        }
        if (permissionGroupInfo.icon == 0) {
            permissionGroupInfo.icon = R.drawable.ic_permission_android;
        }
        return permissionGroupInfo;
    }

    private PermissionGroupInfo getFakePermissionGroupInfo(String packageName) {
        PermissionGroupInfo permissionGroupInfo = new PermissionGroupInfo();
        switch (packageName) {
            case "android":
                permissionGroupInfo.icon = R.drawable.ic_permission_android;
                permissionGroupInfo.name = "android";
                break;
            case "com.google.android.gsf":
            case "com.android.vending":
                permissionGroupInfo.icon = R.drawable.ic_permission_google;
                permissionGroupInfo.name = "google";
                break;
            default:
                permissionGroupInfo.icon = R.drawable.ic_permission_unknown;
                permissionGroupInfo.name = "unknown";
                break;
        }
        return permissionGroupInfo;
    }
}
