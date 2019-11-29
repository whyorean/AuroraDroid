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

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aurora.adroid.GlideApp;
import com.aurora.adroid.R;
import com.aurora.adroid.activity.AuroraActivity;
import com.aurora.adroid.activity.DetailsActivity;
import com.aurora.adroid.model.App;
import com.aurora.adroid.sheet.AppMenuSheet;
import com.aurora.adroid.util.DatabaseUtil;
import com.aurora.adroid.util.PackageUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class InstalledAppsAdapter extends RecyclerView.Adapter<InstalledAppsAdapter.ViewHolder> {

    private List<App> appList = new ArrayList<>();
    private Context context;
    private PackageManager packageManager;
    private AppMenuSheet menuSheet;

    public InstalledAppsAdapter(Context context) {
        this.context = context;
        this.packageManager = context.getPackageManager();
        this.menuSheet = new AppMenuSheet();
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

    public void remove(App app) {
        appList.remove(app);
        notifyDataSetChanged();
    }

    public void clearData() {
        appList.clear();
        notifyDataSetChanged();
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
        List<String> versionList = new ArrayList<>();
        List<String> extraList = new ArrayList<>();

        holder.txtTitle.setText(app.getName());
        getDetails(versionList, extraList, app);
        setText(holder.txtVersion, TextUtils.join(" • ", versionList));
        setText(holder.txtExtra, TextUtils.join(" • ", extraList));

        holder.itemView.setOnClickListener(v -> {
            ActivityOptions activityOptions = ActivityOptions.makeSceneTransitionAnimation((AppCompatActivity) context);
            Intent intent = new Intent(context, DetailsActivity.class);
            intent.putExtra("INTENT_APK_FILE_NAME", app.getPackageName());
            context.startActivity(intent,activityOptions.toBundle());
        });

        holder.itemView.setOnLongClickListener(v -> {
            menuSheet.setApp(app);
            menuSheet.setAdapter(this);
            menuSheet.show(getFragmentManager(), "BOTTOM_MENU_SHEET");
            return false;
        });

        if (app.getIcon() == null)
            holder.imgIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_placeholder));
        else
            GlideApp
                    .with(context)
                    .asBitmap()
                    .load(DatabaseUtil.getImageUrl(app))
                    .placeholder(R.drawable.ic_placeholder)
                    .into(holder.imgIcon);
    }

    private FragmentManager getFragmentManager() {
        return ((AuroraActivity) context).getSupportFragmentManager();
    }

    protected void getDetails(List<String> versionList, List<String> extraList, App app) {
        PackageInfo packageInfo = PackageUtil.getPackageInfo(packageManager, app.getPackageName());
        if (packageInfo != null)
            versionList.add(packageInfo.versionName + "." + packageInfo.versionCode);
        extraList.add(PackageUtil.isSystemApp(packageManager, app.getPackageName()) ?
                "System App"
                : "User App");
    }

    private void setText(TextView textView, String text) {
        if (!TextUtils.isEmpty(text)) {
            textView.setText(text);
            textView.setVisibility(View.VISIBLE);
        } else {
            textView.setVisibility(View.INVISIBLE);
        }
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
        ImageView imgIcon;
        @BindView(R.id.txt_title)
        TextView txtTitle;
        @BindView(R.id.txt_version)
        TextView txtVersion;
        @BindView(R.id.txt_extra)
        TextView txtExtra;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
