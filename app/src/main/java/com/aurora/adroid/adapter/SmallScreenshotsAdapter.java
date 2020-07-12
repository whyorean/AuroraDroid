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

package com.aurora.adroid.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aurora.adroid.Constants;
import com.aurora.adroid.GlideApp;
import com.aurora.adroid.R;
import com.aurora.adroid.ui.generic.activity.FullscreenImageActivity;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.google.gson.Gson;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SmallScreenshotsAdapter extends RecyclerView.Adapter<SmallScreenshotsAdapter.ViewHolder> {

    private Context context;
    private List<String> urlList;

    public SmallScreenshotsAdapter(List<String> urlList, Context context) {
        this.urlList = urlList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_screenshots_small, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        GlideApp
                .with(context)
                .load(urlList.get(position))
                .transforms(new CenterCrop(), new RoundedCorners(15))
                .placeholder(R.drawable.ic_placeholder_screenshots)
                .priority(Priority.HIGH)
                .into(holder.imageView);

        holder.imageView.setOnClickListener(v -> {
            Intent intent = new Intent(context, FullscreenImageActivity.class);
            intent.putExtra(FullscreenImageActivity.INTENT_SCREENSHOT_NUMBER, position);
            intent.putExtra(Constants.STRING_EXTRA, new Gson().toJson(urlList));
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return urlList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.img_screenshot)
        ImageView imageView;

        ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            imageView.getLayoutParams().height = (Resources.getSystem().getDisplayMetrics().heightPixels) / 3;
            imageView.getLayoutParams().width = (Resources.getSystem().getDisplayMetrics().widthPixels) / 3;
        }
    }
}

