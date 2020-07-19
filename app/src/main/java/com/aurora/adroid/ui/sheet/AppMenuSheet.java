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

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.aurora.adroid.AuroraApplication;
import com.aurora.adroid.Constants;
import com.aurora.adroid.R;
import com.aurora.adroid.event.Event;
import com.aurora.adroid.event.EventType;
import com.aurora.adroid.installer.AppInstaller;
import com.aurora.adroid.manager.BlacklistManager;
import com.aurora.adroid.manager.FavouritesManager;
import com.aurora.adroid.model.App;
import com.aurora.adroid.util.ApkCopier;
import com.aurora.adroid.util.Log;
import com.aurora.adroid.util.PackageUtil;
import com.google.android.material.navigation.NavigationView;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class AppMenuSheet extends BaseBottomSheet {

    public static final String TAG = AppMenuSheet.class.getName();

    @BindView(R.id.navigation_view)
    NavigationView navigationView;

    private App app;
    private CompositeDisposable disposable = new CompositeDisposable();

    @Nullable
    @Override
    protected View onCreateContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sheet_app_menu, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    protected void onContentViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onContentViewCreated(view, savedInstanceState);
        if (getArguments() != null) {
            Bundle bundle = getArguments();
            stringExtra = bundle.getString(Constants.STRING_EXTRA);
            intExtra = bundle.getInt(Constants.INT_EXTRA);
            //Get App from bundle
            app = gson.fromJson(stringExtra, App.class);
            setupNavigationView();
        } else {
            dismissAllowingStateLoss();
        }
    }

    private void setupNavigationView() {
        final FavouritesManager favouritesManager = new FavouritesManager(requireContext());
        final BlacklistManager blacklistManager = new BlacklistManager(requireContext());
        //final IgnoreListManager ignoreListManager = new IgnoreListManager(requireContext());

        final boolean isFavourite = favouritesManager.isFavourite(app);
        final boolean isBlacklisted = blacklistManager.isBlacklisted(app.getPackageName());
        //final boolean isIgnored = ignoreListManager.isIgnored(app.getPackageName(), app.getVersionCode());

        //Switch strings for Add/Remove Favourite
        final MenuItem favMenu = navigationView.getMenu().findItem(R.id.action_fav);
        favMenu.setTitle(isFavourite ? R.string.action_favourite_remove : R.string.action_favourite_add);

        //Switch strings for Add/Remove Blacklist
        final MenuItem blackListMenu = navigationView.getMenu().findItem(R.id.action_blacklist);
        blackListMenu.setTitle(isBlacklisted ? R.string.action_whitelist : R.string.action_blacklist);

        //Switch strings for Add/Remove IgnoreList
        //final MenuItem ignoreVersionMenu = navigationView.getMenu().findItem(R.id.action_ignore);
        //ignoreVersionMenu.setTitle(isIgnored ? R.string.action_ignore_remove : R.string.action_ignore);

        //Show/Hide actions based on installed status
        final boolean installed = PackageUtil.isInstalled(requireContext(), app.getPackageName());
        navigationView.getMenu().findItem(R.id.action_uninstall).setVisible(installed);
        navigationView.getMenu().findItem(R.id.action_local).setVisible(installed);
        navigationView.getMenu().findItem(R.id.action_info).setVisible(installed);

        navigationView.setNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.action_fav:
                    if (isFavourite) {
                        favouritesManager.removeFromFavourites(app);
                    } else {
                        favouritesManager.addToFavourites(app);
                    }
                    break;
                case R.id.action_blacklist:
                    if (isBlacklisted) {
                        blacklistManager.removeFromBlacklist(app.getPackageName());
                        AuroraApplication.rxNotify(new Event(EventType.WHITELIST, app.getPackageName()));
                    } else {
                        blacklistManager.addToBlacklist(app.getPackageName());
                        AuroraApplication.rxNotify(new Event(EventType.BLACKLIST, app.getPackageName()));
                    }
                    Toast.makeText(requireContext(), isBlacklisted ?
                                    requireContext().getString(R.string.toast_apk_whitelisted) :
                                    requireContext().getString(R.string.toast_apk_blacklisted),
                            Toast.LENGTH_SHORT).show();
                    break;
                case R.id.action_local:
                    disposable.add(Observable.fromCallable(() -> new ApkCopier(requireContext())
                            .copy(app.getPackageName()))
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(result -> Toast.makeText(requireContext(), result
                                    ? requireContext().getString(R.string.toast_apk_copy_success)
                                    : requireContext().getString(R.string.toast_apk_copy_failure), Toast.LENGTH_SHORT)
                                    .show(), error -> Log.e("Failed to copy app to local directory")));
                    break;
                case R.id.action_uninstall:
                    AppInstaller.getInstance(requireContext())
                            .getDefaultInstaller()
                            .uninstall(app.getPackageName());
                    break;
                case R.id.action_info:
                    try {
                        requireContext().startActivity(new Intent("android.settings.APPLICATION_DETAILS_SETTINGS",
                                Uri.parse("package:" + app.getPackageName())));
                    } catch (ActivityNotFoundException e) {
                        Log.e("Could not find system app activity");
                    }
                    break;
            }
            dismissAllowingStateLoss();
            return false;
        });
    }
}
