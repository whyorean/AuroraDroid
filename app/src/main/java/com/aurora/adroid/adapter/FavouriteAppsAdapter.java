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

package com.aurora.adroid.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aurora.adroid.GlideApp;
import com.aurora.adroid.R;
import com.aurora.adroid.activity.DetailsActivity;
import com.aurora.adroid.manager.FavouriteListManager;
import com.aurora.adroid.model.App;
import com.aurora.adroid.util.DatabaseUtil;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FavouriteAppsAdapter extends RecyclerView.Adapter {

    private List<App> appList = new ArrayList<>();
    private List<App> selectedList = new ArrayList<>();
    private Context context;
    private FavouriteListManager manager;
    private FavouriteViewHolder.ItemClickListener itemClickListener;

    public FavouriteAppsAdapter(Context context, FavouriteViewHolder.ItemClickListener itemClickListener, List<App> appsToAdd) {
        this.itemClickListener = itemClickListener;
        this.context = context;
        manager = new FavouriteListManager(context);
        appList.addAll(appsToAdd);
        Collections.sort(appsToAdd, (App1, App2) ->
                App1.getName().compareTo(App2.getName()));
    }

    public void add(int position, App app) {
        appList.add(position, app);
        notifyItemInserted(position);
    }

    public void add(App app) {
        appList.add(app);
    }

    public void remove(int position) {
        manager.remove(appList.get(position).getPackageName());
        appList.remove(position);
        notifyItemRemoved(position);
    }

    @NonNull
    @Override
    public FavouriteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.item_favorite, parent, false);
        return new FavouriteViewHolder(view, itemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        final FavouriteViewHolder holder = (FavouriteViewHolder) viewHolder;
        final App app = appList.get(position);

        holder.AppTitle.setText(app.getName());

        if (isInstalled(app)) {
            holder.AppExtra.setText(context.getText(R.string.list_installed));
            holder.AppCheckbox.setEnabled(false);
            holder.AppCheckbox.setAlpha(0.5f);
        } else {
            holder.AppExtra.setText(context.getText(R.string.list_not_installd));
        }

        if (app.getIcon() == null)
            holder.AppIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_placeholder));
        else
            GlideApp
                    .with(context)
                    .asBitmap()
                    .load(DatabaseUtil.getImageUrl(app))
                    .placeholder(R.drawable.ic_placeholder)
                    .into(holder.AppIcon);

        holder.AppCheckbox.setChecked(isSelected(appList.get(position)));

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailsActivity.class);
            intent.putExtra("INTENT_APK_FILE_NAME", app.getPackageName());
            context.startActivity(intent);
        });
    }

    private boolean isInstalled(App app) {
        try {
            context.getPackageManager().getPackageInfo(app.getPackageName(), 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public void toggleSelection(int position) {
        App app = appList.get(position);
        if (selectedList.contains(app)) {
            selectedList.remove(app);
        } else {
            selectedList.add(app);
        }
        notifyItemChanged(position);
    }

    private boolean isSelected(App app) {
        return selectedList.contains(app);
    }

    @Override
    public int getItemCount() {
        return appList.size();
    }

    public List<App> getSelectedList() {
        return new ArrayList<>(selectedList);
    }
}
