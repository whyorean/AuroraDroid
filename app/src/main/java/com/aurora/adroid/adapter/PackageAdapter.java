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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aurora.adroid.R;
import com.aurora.adroid.download.DownloadManager;
import com.aurora.adroid.event.Event;
import com.aurora.adroid.event.Events;
import com.aurora.adroid.event.RxBus;
import com.aurora.adroid.model.Package;
import com.aurora.adroid.util.DatabaseUtil;
import com.aurora.adroid.util.Log;
import com.aurora.adroid.util.PackageUtil;
import com.aurora.adroid.util.PathUtil;
import com.aurora.adroid.util.Util;
import com.tonyodev.fetch2.EnqueueAction;
import com.tonyodev.fetch2.Fetch;
import com.tonyodev.fetch2.Request;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PackageAdapter extends RecyclerView.Adapter<PackageAdapter.ViewHolder> {

    private List<Package> packages;
    private Context context;

    public PackageAdapter(Context context, List<Package> packages) {
        this.context = context;
        this.packages = packages;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_package, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final Package pkg = packages.get(position);
        final boolean installed = PackageUtil.isInstalledVersion(context, pkg);
        final boolean isArchDependent = PackageUtil.isArchSpecificPackage(pkg);
        holder.txtApkVersion.setText(new StringBuilder()
                .append(pkg.getVersionName())
                .append(".")
                .append(pkg.getVersionCode()));
        holder.imgInstalled.setVisibility(installed ? View.VISIBLE : View.GONE);
        holder.imgDownload.setVisibility(installed ? View.GONE : View.VISIBLE);
        holder.txtApkSize.setVisibility(installed ? View.GONE : View.VISIBLE);
        holder.txtApkArch.setText(isArchDependent ? pkg.getNativecode().get(0) : "Universal");
        holder.txtApkRepo.setText(pkg.getRepoName());
        holder.txtApkAdded.setText(Util.getDateFromMilli(pkg.getAdded()));
        holder.txtApkSize.setText(Util.humanReadableByteValue(pkg.getSize(), true));
        holder.imgDownload.setOnClickListener(v -> {
            final String apkName = pkg.getApkName();
            Request request = new Request(DatabaseUtil.getDownloadURl(pkg), PathUtil.getApkPath(context, apkName));
            request.setEnqueueAction(EnqueueAction.REPLACE_EXISTING);
            request.setTag(pkg.getPackageName());
            Fetch fetch = DownloadManager.getFetchInstance(context);
            fetch.enqueue(request, updatedRequest -> {
                Log.i("Downloading : %s", pkg.getApkName());
                RxBus.publish(new Event(Events.DOWNLOAD_INITIATED));
            }, error -> {
                Log.e("Unknown error occurred");
                RxBus.publish(new Event(Events.DOWNLOAD_FAILED));
            });
        });
    }

    @Override
    public int getItemCount() {
        return packages.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.img_download)
        ImageView imgDownload;
        @BindView(R.id.img_installed)
        ImageView imgInstalled;
        @BindView(R.id.txt_apk_version)
        TextView txtApkVersion;
        @BindView(R.id.txt_apk_repo)
        TextView txtApkRepo;
        @BindView(R.id.txt_apk_added)
        TextView txtApkAdded;
        @BindView(R.id.txt_apk_arch)
        TextView txtApkArch;
        @BindView(R.id.txt_apk_size)
        TextView txtApkSize;

        ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}

