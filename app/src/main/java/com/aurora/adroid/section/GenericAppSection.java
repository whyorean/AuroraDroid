package com.aurora.adroid.section;

import android.content.Context;
import android.content.Intent;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.aurora.adroid.GlideApp;
import com.aurora.adroid.R;
import com.aurora.adroid.Sort;
import com.aurora.adroid.ui.activity.DetailsActivity;
import com.aurora.adroid.model.App;
import com.aurora.adroid.util.DatabaseUtil;
import com.aurora.adroid.util.TextUtil;
import com.aurora.adroid.util.Util;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.luizgrp.sectionedrecyclerviewadapter.Section;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters;

import static android.graphics.Typeface.BOLD;

public class GenericAppSection extends Section implements FilterableSection {

    private Context context;
    private List<App> appList;
    private List<App> filteredList = new ArrayList<>();
    private String searchQuery = null;

    public GenericAppSection(Context context, List<App> appList) {
        super(SectionParameters.builder()
                .itemResourceId(R.layout.item_installed)
                .build());
        this.context = context;
        this.appList = appList;
        this.filteredList.addAll(appList);
    }

    public void sortBy(Sort sort) {
        switch (sort) {
            case NAME_AZ:
                Collections.sort(this.filteredList, (App1, App2) ->
                        App1.getName().compareToIgnoreCase(App2.getName()));
                break;
            case NAME_ZA:
                Collections.sort(this.filteredList, (App1, App2) ->
                        App2.getName().compareToIgnoreCase(App1.getName()));
                break;
            case SIZE_MIN:
                Collections.sort(this.filteredList, (App1, App2) ->
                        App1.getAppPackage().getSize().compareTo(App2.getAppPackage().getSize()));
                break;
            case SIZE_MAX:
                Collections.sort(this.filteredList, (App1, App2) ->
                        App2.getAppPackage().getSize().compareTo(App1.getAppPackage().getSize()));
                break;
            case DATE_UPDATED:
                Collections.sort(this.filteredList, (App1, App2) ->
                        App2.getLastUpdated().compareTo(App1.getLastUpdated()));
                break;
            case DATE_ADDED:
                Collections.sort(this.filteredList, (App1, App2) ->
                        App2.getAdded().compareTo(App1.getAdded()));
                break;
        }
    }

    @Override
    public int getContentItemsTotal() {
        return filteredList.size();
    }

    @Override
    public RecyclerView.ViewHolder getItemViewHolder(View view) {
        return new ContentHolder(view);
    }

    @Override
    public void onBindItemViewHolder(RecyclerView.ViewHolder holder, int position) {
        final ContentHolder contentHolder = (ContentHolder) holder;
        final App app = filteredList.get(position);
        final List<String> extraStringList = new ArrayList<>();

        extraStringList.add(Util.getDateFromMilli(app.getLastUpdated()));
        extraStringList.add(app.getRepoName());
        if (!app.getAuthorName().equals("unknown"))
            extraStringList.add(app.getAuthorName());

        contentHolder.txtTitle.setText(app.getName());
        contentHolder.txtVersion.setText(TextUtils.join(" • ", extraStringList));

        String summary;
        if (app.getLocalized() != null
                && app.getLocalized().getEnUS() != null
                && app.getLocalized().getEnUS().getSummary() != null) {
            summary = TextUtil.emptyIfNull(app.getLocalized().getEnUS().getSummary());
        } else
            summary = TextUtil.emptyIfNull(app.getSummary());

        summary = StringUtils.capitalize(summary);

        contentHolder.txtExtra.setText(summary);
        contentHolder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailsActivity.class);
            intent.putExtra(DetailsActivity.INTENT_PACKAGE_NAME, app.getPackageName());
            intent.putExtra(DetailsActivity.INTENT_REPO_NAME, app.getRepoName());
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

        if (!StringUtils.isEmpty(searchQuery)) {
            String appName = app.getName();
            Pattern word = Pattern.compile(searchQuery, Pattern.CASE_INSENSITIVE);
            Matcher match = word.matcher(appName.toLowerCase());

            if (match.find()) {
                SpannableString spannable = new SpannableString(appName);
                spannable.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.colorAccent)),
                        match.start(),
                        match.end(),
                        Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                spannable.setSpan(new StyleSpan(BOLD),
                        match.start(),
                        match.end(),
                        Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                contentHolder.txtTitle.setText(spannable);
            }
        }

        if (app.getLocalized() != null
                && app.getLocalized().getEnUS() != null
                && app.getLocalized().getEnUS().getSummary() != null) {
            summary = TextUtil.emptyIfNull(app.getLocalized().getEnUS().getSummary());
        } else
            summary = TextUtil.emptyIfNull(app.getSummary());

        if (!StringUtils.isEmpty(searchQuery) && !summary.isEmpty()) {
            Pattern word = Pattern.compile(searchQuery);
            Matcher match = word.matcher(summary.toLowerCase());

            if (match.find()) {
                SpannableString spannable = new SpannableString(summary);
                spannable.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.colorAccent)),
                        match.start(),
                        match.end(),
                        Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                spannable.setSpan(new StyleSpan(BOLD),
                        match.start(),
                        match.end(),
                        Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                contentHolder.txtExtra.setText(spannable);
            }
        }
    }

    @Override
    public void filter(@NonNull String query) {
        searchQuery = query;
        if (TextUtils.isEmpty(query)) {
            filteredList.clear();
            filteredList.addAll(appList);
            setVisible(true);
        } else {
            filteredList.clear();
            for (final App app : appList) {

                if ((app.getName().toLowerCase()).contains(query.toLowerCase())) {
                    filteredList.add(app);
                    continue;
                }

                String summary;
                if (app.getLocalized() != null
                        && app.getLocalized().getEnUS() != null
                        && app.getLocalized().getEnUS().getSummary() != null) {
                    summary = TextUtil.emptyIfNull(app.getLocalized().getEnUS().getSummary());
                } else
                    summary = TextUtil.emptyIfNull(app.getSummary());

                if (!summary.isEmpty() && summary.toLowerCase().contains(query)) {
                    filteredList.add(app);
                }
            }
            setVisible(!filteredList.isEmpty());
        }
    }

    public static class ContentHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.img)
        AppCompatImageView imgIcon;
        @BindView(R.id.line1)
        AppCompatTextView txtTitle;
        @BindView(R.id.line2)
        AppCompatTextView txtVersion;
        @BindView(R.id.line3)
        AppCompatTextView txtExtra;

        ContentHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
