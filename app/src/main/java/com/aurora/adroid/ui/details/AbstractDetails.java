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

package com.aurora.adroid.ui.details;

import android.content.Context;
import android.text.TextUtils;
import android.widget.TextView;

import com.aurora.adroid.model.App;
import com.aurora.adroid.util.ViewUtil;

import butterknife.ButterKnife;

public abstract class AbstractDetails {

    protected DetailsActivity activity;
    protected App app;
    protected Context context;

    public AbstractDetails(DetailsActivity activity, App app) {
        this.activity = activity;
        this.context = activity;
        this.app = app;
        ButterKnife.bind(this, activity);
    }

    public App getApp() {
        return app;
    }

    public void setApp(App app) {
        this.app = app;
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
