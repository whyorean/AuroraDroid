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

package com.aurora.adroid.ui.generic.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;

import com.aurora.adroid.R;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Modifier;

public abstract class BaseFragment extends Fragment {

    protected boolean awaiting = false;
    protected Gson gson = new GsonBuilder().excludeFieldsWithModifiers(Modifier.TRANSIENT).create();

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    protected void showSnackBar(View view, @StringRes int message, int duration, View.OnClickListener onClickListener) {
        Snackbar snackbar = Snackbar.make(view, message, duration);
        if (onClickListener != null)
            snackbar.setAction(R.string.action_retry, onClickListener);
        snackbar.show();
    }

    protected void showSnackBar(View view, @StringRes int message, View.OnClickListener onClickListener) {
        showSnackBar(view, message, 0, onClickListener);
    }
}
