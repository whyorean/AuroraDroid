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

package com.aurora.adroid.fragment;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ViewSwitcher;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;

import com.aurora.adroid.ErrorType;
import com.aurora.adroid.R;
import com.aurora.adroid.view.ErrorView;

import butterknife.BindView;

public abstract class BaseFragment extends Fragment {

    @BindView(R.id.coordinator)
    CoordinatorLayout coordinatorLayout;
    @BindView(R.id.view_switcher)
    ViewSwitcher viewSwitcher;
    @BindView(R.id.content_view)
    ViewGroup layoutContent;
    @BindView(R.id.err_view)
    ViewGroup layoutError;
    private Context context;

    protected abstract View.OnClickListener errRetry();

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    /*ErrorView UI handling methods*/

    protected void setErrorView(ErrorType errorType) {
        layoutError.removeAllViews();
        layoutError.addView(new ErrorView(context, errorType, getAction(errorType)));
    }

    protected void switchViews(boolean showError) {
        if (viewSwitcher.getCurrentView() == layoutContent && showError)
            viewSwitcher.showNext();
        else if (viewSwitcher.getCurrentView() == layoutError && !showError)
            viewSwitcher.showPrevious();
    }


    protected View.OnClickListener errClose() {
        return v -> {

        };
    }

    private View.OnClickListener getAction(ErrorType errorType) {
        switch (errorType) {
            case MALFORMED:
                return errClose();
            case NO_INSTALLED_APPS:
            case NO_APPS:
            case NO_UPDATES:
                return errRetry();
            default:
                return null;
        }
    }
}
