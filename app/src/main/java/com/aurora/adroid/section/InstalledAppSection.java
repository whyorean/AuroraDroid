package com.aurora.adroid.section;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.aurora.adroid.GlideApp;
import com.aurora.adroid.R;
import com.aurora.adroid.activity.DetailsActivity;
import com.aurora.adroid.model.App;
import com.aurora.adroid.util.DatabaseUtil;
import com.aurora.adroid.util.PackageUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.luizgrp.sectionedrecyclerviewadapter.Section;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters;

public class InstalledAppSection extends Section {

    private Context context;
    private List<App> appList;

    protected PackageManager packageManager;

    public InstalledAppSection(Context context, List<App> appList) {
        super(SectionParameters.builder()
                .itemResourceId(R.layout.item_installed)
                .build());
        this.context = context;
        this.appList = appList;
        this.packageManager = context.getPackageManager();
    }

    @Override
    public int getContentItemsTotal() {
        return appList.size();
    }

    @Override
    public RecyclerView.ViewHolder getItemViewHolder(View view) {
        return new ContentHolder(view);
    }

    @Override
    public void onBindItemViewHolder(RecyclerView.ViewHolder holder, int position) {
        final ContentHolder contentHolder = (ContentHolder) holder;
        final App app = appList.get(position);

        List<String> versionList = new ArrayList<>();
        List<String> extraList = new ArrayList<>();

        contentHolder.txtTitle.setText(app.getName());
        getDetails(versionList, extraList, app);
        setText(contentHolder.txtVersion, TextUtils.join(" • ", versionList));
        setText(contentHolder.txtExtra, TextUtils.join(" • ", extraList));

        contentHolder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailsActivity.class);
            intent.putExtra(DetailsActivity.INTENT_PACKAGE_NAME, app.getPackageName());
            context.startActivity(intent);
        });

        if (app.getIcon() == null)
            contentHolder.imgIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_placeholder));
        else
            GlideApp
                    .with(context)
                    .asBitmap()
                    .load(DatabaseUtil.getImageUrl(app))
                    .placeholder(R.drawable.ic_placeholder)
                    .into(contentHolder.imgIcon);
    }

    private void getDetails(List<String> versionList, List<String> extraList, App app) {
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

    public static class ContentHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.img_icon)
        AppCompatImageView imgIcon;
        @BindView(R.id.txt_title)
        AppCompatTextView txtTitle;
        @BindView(R.id.txt_version)
        AppCompatTextView txtVersion;
        @BindView(R.id.txt_extra)
        AppCompatTextView txtExtra;

        ContentHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
