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

package com.aurora.adroid.sheet;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.aurora.adroid.R;
import com.aurora.adroid.adapter.InstalledAppsAdapter;
import com.aurora.adroid.adapter.UpdatableAppsAdapter;
import com.aurora.adroid.installer.Installer;
import com.aurora.adroid.manager.BlacklistManager;
import com.aurora.adroid.manager.FavouriteListManager;
import com.aurora.adroid.model.App;
import com.aurora.adroid.util.ApkCopier;
import com.aurora.adroid.util.PackageUtil;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AppMenuSheet extends BottomSheetDialogFragment {

    @BindView(R.id.menu_title)
    TextView txtTitle;
    @BindView(R.id.btn_fav)
    MaterialButton btnFav;
    @BindView(R.id.btn_blacklist)
    MaterialButton btnBlacklist;
    @BindView(R.id.btn_local_apk)
    MaterialButton btnLocal;
    @BindView(R.id.btn_manual)
    MaterialButton btnManual;
    @BindView(R.id.btn_uninstall)
    MaterialButton btnUninstall;

    private App app;
    private Context context;
    private RecyclerView.Adapter adapter;

    public AppMenuSheet() {
    }

    public void setAdapter(RecyclerView.Adapter adapter) {
        this.adapter = adapter;
    }

    public App getApp() {
        return app;
    }

    public void setApp(App app) {
        this.app = app;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sheet_app_menu, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final boolean isInstalled = PackageUtil.isInstalled(context, app.getPackageName());
        txtTitle.setText(app.getName());

        btnUninstall.setVisibility(isInstalled ? View.VISIBLE : View.GONE);
        btnLocal.setVisibility(isInstalled ? View.VISIBLE : View.GONE);

        final FavouriteListManager favouriteListManager = new FavouriteListManager(context);
        boolean isFav = favouriteListManager.contains(app.getPackageName());
        btnFav.setText(isFav ? R.string.action_favourite_remove : R.string.action_favourite_add);
        btnFav.setOnClickListener(v -> {
            if (isFav) {
                favouriteListManager.remove(app.getPackageName());
            } else {
                favouriteListManager.add(app.getPackageName());
            }
            dismissAllowingStateLoss();
        });

        final BlacklistManager blacklistManager = new BlacklistManager(context);
        boolean isBlacklisted = blacklistManager.contains(app.getPackageName());
        btnBlacklist.setText(isBlacklisted ? R.string.action_whitelist : R.string.action_blacklist);
        btnBlacklist.setOnClickListener(v -> {
            if (isBlacklisted) {
                blacklistManager.remove(app.getPackageName());
                Toast.makeText(context, context.getString(R.string.toast_apk_whitelisted),
                        Toast.LENGTH_SHORT).show();
            } else {
                blacklistManager.add(app.getPackageName());
                Toast.makeText(context, context.getString(R.string.toast_apk_blacklisted),
                        Toast.LENGTH_SHORT).show();
                if (adapter instanceof InstalledAppsAdapter)
                    ((InstalledAppsAdapter) adapter).remove(app);
                if (adapter instanceof UpdatableAppsAdapter)
                    ((UpdatableAppsAdapter) adapter).remove(app);
            }
            dismissAllowingStateLoss();
        });

        btnLocal.setOnClickListener(v -> {
            final ApkCopier apkCopier = new ApkCopier(context);
            boolean success = apkCopier.copy(app);
            Toast.makeText(context, success
                    ? context.getString(R.string.toast_apk_copy_success)
                    : context.getString(R.string.toast_apk_copy_failure), Toast.LENGTH_SHORT)
                    .show();
            dismissAllowingStateLoss();
        });

        btnManual.setOnClickListener(v -> {

        });

        btnUninstall.setOnClickListener(v -> {
            new Installer(context).uninstall(app);
            dismissAllowingStateLoss();
        });
    }
}
