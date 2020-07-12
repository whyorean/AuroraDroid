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

package com.aurora.adroid.ui.sheet;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.text.HtmlCompat;

import com.aurora.adroid.Constants;
import com.aurora.adroid.R;
import com.aurora.adroid.model.App;
import com.aurora.adroid.util.LocalizationUtil;

import org.apache.commons.lang3.StringUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MoreInfoSheet extends BaseBottomSheet {

    public static final String TAG = MoreInfoSheet.class.getName();

    @BindView(R.id.txt_changelog)
    TextView txtChangelog;
    @BindView(R.id.txt_description)
    TextView txtDescription;
    @BindView(R.id.txt_anti_desc)
    TextView txtAntiFeatures;

    @Override
    public View onCreateContentView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sheet_read_more, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onContentViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final Bundle bundle = getArguments();
        if (bundle != null) {
            String rawApp = bundle.getString(Constants.STRING_EXTRA);
            if (rawApp != null) {
                App app = gson.fromJson(rawApp, App.class);
                String description = LocalizationUtil.getLocalizedDescription(requireContext(), app);
                description = description.replace("\n", "<br>");
                txtDescription.setText(HtmlCompat.fromHtml(description, HtmlCompat.FROM_HTML_OPTION_USE_CSS_COLORS));
                txtChangelog.setText(LocalizationUtil.getLocalizedChangelog(requireContext(), app));
                txtAntiFeatures.setText(getAntiFeatures(app));
            }
        } else {
            dismissAllowingStateLoss();
        }
    }

    private String getAntiFeatures(App app) {
        if (app.getAntiFeatures() != null && !app.getAntiFeatures().isEmpty()) {
            return StringUtils.join(app.getAntiFeatures(), "\n");
        } else {
            return requireContext().getString(R.string.details_no_anti_features);
        }
    }
}
