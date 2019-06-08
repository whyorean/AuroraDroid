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
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aurora.adroid.GlideApp;
import com.aurora.adroid.R;
import com.aurora.adroid.activity.DetailsActivity;
import com.aurora.adroid.activity.DownloadsActivity;
import com.aurora.adroid.download.DownloadManager;
import com.aurora.adroid.sheet.DownloadMenuSheet;
import com.aurora.adroid.util.PackageUtil;
import com.aurora.adroid.util.Util;
import com.aurora.adroid.util.ViewUtil;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.tonyodev.fetch2.AbstractFetchListener;
import com.tonyodev.fetch2.Download;
import com.tonyodev.fetch2.Error;
import com.tonyodev.fetch2.Fetch;
import com.tonyodev.fetch2.FetchListener;
import com.tonyodev.fetch2.Status;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DownloadsAdapter extends RecyclerView.Adapter<DownloadsAdapter.ViewHolder> {

    private List<Download> downloadList = new ArrayList<>();
    private Context context;
    private Fetch fetch;
    private DownloadMenuSheet menuSheet;

    public DownloadsAdapter(Context context) {
        this.context = context;
        this.menuSheet = new DownloadMenuSheet();
        fetch = DownloadManager.getFetchInstance(context);
        fetchData();
    }

    private void fetchData() {
        fetch.getDownloads(fetchDownloadList -> {
            downloadList = fetchDownloadList;
            notifyDataSetChanged();
        });
    }

    public void refreshList() {
        fetchData();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.item_downloads, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        final Download download = downloadList.get(position);
        final String displayName = PackageUtil.getAppDisplayName(context, download.getTag());
        final String iconURL = PackageUtil.getIconURL(context, download.getTag());

        fetch.addListener(getFetchListener(download.getId(), viewHolder));

        GlideApp
                .with(context)
                .load(iconURL)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .transition(new DrawableTransitionOptions().crossFade())
                .into(viewHolder.imgDownload);

        viewHolder.txtTitle.setText(displayName);
        viewHolder.txtStatus.setText(Util.getStatus(download.getStatus()));
        viewHolder.txtSize.setText(new StringBuilder()
                .append(Util.humanReadableByteValue(download.getDownloaded(), true))
                .append("/")
                .append(Util.humanReadableByteValue(download.getTotal(), true)));
        viewHolder.txtProgress.setText(new StringBuilder().append(download.getProgress()).append("%"));
        viewHolder.progressBar.setProgress(download.getProgress());

        if (download.getStatus() == Status.COMPLETED) {
            viewHolder.txtETA.setText("");
            viewHolder.txtSpeed.setText("");
        } else if (download.getStatus() != Status.DOWNLOADING) {
            viewHolder.txtETA.setText("N/A");
            viewHolder.txtSpeed.setText("--/s");
        }

        viewHolder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailsActivity.class);
            intent.putExtra("INTENT_APK_FILE_NAME", download.getTag());
            context.startActivity(intent);
        });

        viewHolder.itemView.setOnLongClickListener(v -> {
            menuSheet.setTitle(displayName);
            menuSheet.setDownload(download);
            menuSheet.setDownloadsAdapter(this);
            menuSheet.show(((DownloadsActivity) context).getSupportFragmentManager(), "DOWNLOAD_SHEET");
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return downloadList.size();
    }

    private FetchListener getFetchListener(int currentId, ViewHolder viewHolder) {
        return new AbstractFetchListener() {

            @Override
            public void onResumed(@NotNull Download download) {
                if (currentId == download.getId())
                    viewHolder.txtStatus.setText(Util.getStatus(download.getStatus()));
            }

            @Override
            public void onQueued(@NotNull Download download, boolean waitingOnNetwork) {
                if (currentId == download.getId())
                    viewHolder.txtStatus.setText(Util.getStatus(download.getStatus()));
            }

            @Override
            public void onProgress(@NotNull Download download, long etaInMilliSeconds, long downloadedBytesPerSecond) {
                if (currentId == download.getId()) {
                    viewHolder.progressBar.setProgress(download.getProgress());
                    viewHolder.txtSpeed.setText(Util.humanReadableByteSpeed(downloadedBytesPerSecond, true));
                    viewHolder.txtProgress.setText(new StringBuilder().append(download.getProgress()).append("%"));
                    viewHolder.txtETA.setText(Util.getETAString(context, etaInMilliSeconds));
                    viewHolder.txtSize.setText(new StringBuilder()
                            .append(Util.humanReadableByteValue(download.getDownloaded(), true))
                            .append("/")
                            .append(Util.humanReadableByteValue(download.getTotal(), true)));
                }
            }

            @Override
            public void onPaused(@NotNull Download download) {
                if (currentId == download.getId())
                    viewHolder.txtStatus.setText(Util.getStatus(download.getStatus()));
            }

            @Override
            public void onError(@NotNull Download download, @NotNull Error error, @Nullable Throwable throwable) {
                if (currentId == download.getId()) {
                    viewHolder.txtStatus.setText(Util.getStatus(download.getStatus()));
                    viewHolder.clearStatus();
                }
            }

            @Override
            public void onCompleted(@NotNull Download download) {
                if (currentId == download.getId()) {
                    viewHolder.txtStatus.setText(Util.getStatus(download.getStatus()));
                    ViewUtil.hideWithAnimation(viewHolder.txtSpeed);
                    ViewUtil.hideWithAnimation(viewHolder.txtETA);
                }
            }

            @Override
            public void onCancelled(@NotNull Download download) {
                if (currentId == download.getId()) {
                    viewHolder.txtStatus.setText(Util.getStatus(download.getStatus()));
                    viewHolder.clearStatus();
                }
            }
        };
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.img_download)
        ImageView imgDownload;
        @BindView(R.id.txt_title)
        TextView txtTitle;
        @BindView(R.id.txt_status)
        TextView txtStatus;
        @BindView(R.id.txt_size)
        TextView txtSize;
        @BindView(R.id.txt_progress)
        TextView txtProgress;
        @BindView(R.id.txt_eta)
        TextView txtETA;
        @BindView(R.id.txt_speed)
        TextView txtSpeed;
        @BindView(R.id.progress_download)
        ProgressBar progressBar;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void clearStatus() {
            txtProgress.setText("");
            progressBar.setProgress(0);
            txtSpeed.setText("--/s");
            txtETA.setText("N/A");
            txtSize.setText("--");
        }
    }
}
