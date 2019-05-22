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

package com.aurora.adroid.fragment.details;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.aurora.adroid.fragment.DetailsFragment;
import com.aurora.adroid.model.App;
import com.aurora.adroid.util.ViewUtil;

import butterknife.ButterKnife;

public abstract class AbstractDetails {

    protected DetailsFragment fragment;
    protected App app;
    protected View view;
    protected Context context;

    public AbstractDetails(DetailsFragment fragment, App app) {
        this.fragment = fragment;
        this.app = app;
        this.context = fragment.getContext();
        this.view = fragment.getView();
        ButterKnife.bind(this, view);
    }

    abstract public void draw();

    protected void setText(TextView textView, String text) {
        if (TextUtils.isEmpty(text))
            ViewUtil.hideWithAnimation(textView);
        else {
            ViewUtil.showWithAnimation(textView);
            textView.setText(text);
        }
    }
}
