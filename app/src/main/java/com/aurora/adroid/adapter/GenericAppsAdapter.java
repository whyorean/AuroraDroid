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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aurora.adroid.GlideApp;
import com.aurora.adroid.R;
import com.aurora.adroid.Sort;
import com.aurora.adroid.activity.AuroraActivity;
import com.aurora.adroid.activity.DetailsActivity;
import com.aurora.adroid.model.App;
import com.aurora.adroid.model.Package;
import com.aurora.adroid.util.DatabaseUtil;
import com.aurora.adroid.util.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.disposables.CompositeDisposable;

public class GenericAppsAdapter extends RecyclerView.Adapter<GenericAppsAdapter.ViewHolder> {

    private List<App> appList = new ArrayList<>();
    private Context context;
    private CompositeDisposable disposable = new CompositeDisposable();

    public GenericAppsAdapter(Context context) {
        this.context = context;

    }

    public void add(int position, App app) {
        appList.add(position, app);
        notifyItemInserted(position);
    }

    public void add(App app) {
        appList.add(app);
    }

    public void addData(List<App> appList) {
        this.appList.clear();
        this.appList = appList;
        Collections.sort(this.appList, (App1, App2) -> App1.getName().compareTo(App2.getName()));
        notifyDataSetChanged();
    }

    public void remove(int position) {
        appList.remove(position);
        notifyItemRemoved(position);
    }

    public void clearData() {
        appList.clear();
        notifyDataSetChanged();
    }

    public void sortBy(Sort sort) {
        switch (sort) {
            case NAME_AZ:
                Collections.sort(this.appList, (App1, App2) ->
                        App1.getName().compareTo(App2.getName()));
                notifyDataSetChanged();
                break;
            case NAME_ZA:
                Collections.sort(this.appList, (App1, App2) ->
                        App2.getName().compareTo(App1.getName()));
                notifyDataSetChanged();
                break;
            case SIZE_MIN:
                Collections.sort(this.appList, (App1, App2) ->
                        App1.getAppPackage().getSize().compareTo(App2.getAppPackage().getSize()));
                notifyDataSetChanged();
                break;
            case SIZE_MAX:
                Collections.sort(this.appList, (App1, App2) ->
                        App2.getAppPackage().getSize().compareTo(App1.getAppPackage().getSize()));
                notifyDataSetChanged();
                break;
            case DATE:
                Collections.sort(this.appList, (App1, App2) ->
                        App1.getLastUpdated().compareTo(App2.getLastUpdated()));
                notifyDataSetChanged();
                break;
        }
    }

    public boolean isDataEmpty() {
        return appList.isEmpty();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.item_installed, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final App app = appList.get(position);
        final Package pkg = app.getAppPackage();
        holder.AppTitle.setText(app.getName());
        holder.AppVersion.setText(new StringBuilder()
                .append(pkg.getVersionName())
                .append(".")
                .append(pkg.getVersionCode()));
        holder.AppExtra.setText(Util.humanReadableByteValue(pkg.getSize(), true));
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailsActivity.class);
            intent.putExtra("INTENT_APK_FILE_NAME", app.getPackageName());
            context.startActivity(intent);
        });
        GlideApp
                .with(context)
                .asBitmap()
                .load(DatabaseUtil.getImageUrl(app))
                .placeholder(R.drawable.ic_placeholder)
                .into(holder.AppIcon);
    }

    private FragmentManager getFragmentManager() {
        return ((AuroraActivity) context).getSupportFragmentManager();
    }

    @Override
    public int getItemCount() {
        return appList.size();
    }

    @Override
    public long getItemId(int position) {
        return appList.get(position).hashCode();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.img_icon)
        ImageView AppIcon;
        @BindView(R.id.txt_title)
        TextView AppTitle;
        @BindView(R.id.txt_version)
        TextView AppVersion;
        @BindView(R.id.txt_extra)
        TextView AppExtra;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

}
