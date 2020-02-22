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
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.RecyclerView;

import com.aurora.adroid.R;
import com.aurora.adroid.activity.GenericAppActivity;
import com.aurora.adroid.util.ThemeUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CategoriesAdapter extends RecyclerView.Adapter<CategoriesAdapter.ViewHolder> {

    private List<String> categoryList = new ArrayList<>();
    private Context context;
    private boolean isTransparent;

    private Integer[] categoriesImg = {
            R.drawable.ic_connectivity,
            R.drawable.ic_developement,
            R.drawable.ic_games,
            R.drawable.ic_graphics,
            R.drawable.ic_internet,
            R.drawable.ic_money,
            R.drawable.ic_multimedia,
            R.drawable.ic_navigation,
            R.drawable.ic_phone_sms,
            R.drawable.ic_reading,
            R.drawable.ic_education,
            R.drawable.ic_security,
            R.drawable.ic_sports,
            R.drawable.ic_system,
            R.drawable.ic_theme,
            R.drawable.ic_time,
            R.drawable.ic_writings,
    };

    private int[] colorShades;

    public CategoriesAdapter(Context context) {
        this.context = context;
        this.colorShades = context.getResources().getIntArray(R.array.colorShades);
        this.isTransparent = ThemeUtil.isTransparentStyle(context);
    }

    public void clearData() {
        categoryList.clear();
    }

    public void addData(List<String> categoryList) {
        this.categoryList.clear();
        this.categoryList = categoryList;
        Collections.sort(categoryList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, final int position) {
        @DrawableRes final int imageCat = categoriesImg[position];
        @ColorInt final int color = colorShades[position % colorShades.length];
        final String categoryName = categoryList.get(position);

        viewHolder.itemView.setBackgroundTintList(ColorStateList.valueOf(
                ColorUtils.setAlphaComponent(color, isTransparent ? 60 : 255)));
        viewHolder.txtCat.setText(categoryName);
        viewHolder.txtCat.setTextColor(isTransparent ? color : Color.WHITE);
        viewHolder.imgCat.setImageDrawable(context.getDrawable(imageCat));
        viewHolder.imgCat.setColorFilter(isTransparent ? color : Color.WHITE);

        viewHolder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, GenericAppActivity.class);
            intent.putExtra("LIST_TYPE", 2);
            intent.putExtra("CATEGORY_NAME", categoryName);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.cat_icon)
        ImageView imgCat;
        @BindView(R.id.cat_txt)
        TextView txtCat;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
