/*
 * Warden
 * Copyright (C) 2020, Rahul Kumar Patel <whyorean@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package com.aurora.adroid.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.res.ResourcesCompat;

import com.aurora.adroid.R;
import com.aurora.adroid.util.ViewUtil;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MultiTextLayout extends RelativeLayout {

    @BindView(R.id.txt_primary)
    AppCompatTextView txtPrimary;
    @BindView(R.id.txt_secondary)
    AppCompatTextView txtSecondary;
    @BindView(R.id.divider)
    View divider;

    public MultiTextLayout(@NonNull Context context) {
        super(context);
        init(context, null);
    }

    public MultiTextLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public MultiTextLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public MultiTextLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(@NonNull Context context, @Nullable AttributeSet attrs) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.layout_multitext, this);
        ButterKnife.bind(this, view);

        final TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MultiTextLayout);
        String textPrimary = typedArray.getString(R.styleable.MultiTextLayout_txtPrimary);
        String textSecondary = typedArray.getString(R.styleable.MultiTextLayout_txtSecondary);

        int colorPrimary = typedArray.getColor(R.styleable.MultiTextLayout_txtColorPrimary,
                ViewUtil.getStyledAttribute(context, android.R.attr.textColorPrimary));
        int colorSecondary = typedArray.getColor(R.styleable.MultiTextLayout_txtColorSecondary,
                ViewUtil.getStyledAttribute(context, android.R.attr.textColorPrimary));
        int colorDivider = typedArray.getColor(R.styleable.MultiTextLayout_colorDivider,
                ViewUtil.getStyledAttribute(context, R.attr.colorAccent));

        txtPrimary.setText(textPrimary);
        txtPrimary.setTextColor(colorPrimary);
        txtSecondary.setText(textSecondary);
        txtSecondary.setTextColor(colorSecondary);
        divider.setBackgroundColor(colorDivider);

        typedArray.recycle();
    }

    public void setTxtPrimary(String text) {
        txtPrimary.setText(text);
        invalidate();
    }

    public void setTxtSecondary(String text) {
        //ViewUtil.switchToolbarText(getContext(), txtSecondary, text);
        txtSecondary.setText(text);
        invalidate();
    }

    public void setTxtPrimaryColor(int color) {
        txtPrimary.setTextColor(color);
        invalidate();
    }

    public void setTxtSecondaryColor(int color) {
        txtSecondary.setTextColor(color);
        invalidate();
    }
}
