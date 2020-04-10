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

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.aurora.adroid.R;
import com.aurora.adroid.manager.RepoListManager;
import com.aurora.adroid.model.Repo;
import com.aurora.adroid.ui.activity.QRActivity;
import com.aurora.adroid.util.Util;
import com.google.android.material.textfield.TextInputEditText;

import org.apache.commons.lang3.StringUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RepoAddSheet extends BaseBottomSheet {

    public static final String TAG = "REPO_ADD_SHEET";

    @BindView(R.id.repo_name)
    TextInputEditText inpRepoName;
    @BindView(R.id.repo_url)
    TextInputEditText inpRepoUrl;
    @BindView(R.id.repo_fingerprint)
    TextInputEditText inpFingerprint;

    private RepoListManager repoListManager;

    @Nullable
    @Override
    protected View onCreateContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sheet_repo_add, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    protected void onContentViewCreated(View view, @Nullable Bundle savedInstanceState) {
        repoListManager = new RepoListManager(requireContext());
    }

    @OnClick(R.id.img_scan_qr)
    public void scanQR() {
        Intent intent = new Intent(requireContext(), QRActivity.class);
        ActivityOptions activityOptions = ActivityOptions.makeSceneTransitionAnimation(requireActivity());
        startActivity(intent, activityOptions.toBundle());
        dismissAllowingStateLoss();
    }

    @OnClick(R.id.btn_positive)
    public void addRepo() {
        saveRepoToCustomList();
    }

    @OnClick(R.id.btn_negative)
    public void cancelRepo() {
        dismissAllowingStateLoss();
    }

    private void saveRepoToCustomList() {
        String repoId = String.valueOf(System.currentTimeMillis());
        String repoName = StringUtils.EMPTY;
        String repoUrl = StringUtils.EMPTY;
        String repoFingerPrint = StringUtils.EMPTY;

        if (inpRepoName.getText() == null || inpRepoName.getText().toString().isEmpty())
            inpRepoName.setError("Required");
        else
            repoName = Util.emptyIfNull(inpRepoName.getText().toString());

        if (inpRepoUrl.getText() == null || inpRepoUrl.getText().toString().isEmpty())
            inpRepoUrl.setError("Required");
        else
            repoUrl = Util.emptyIfNull(inpRepoUrl.getText().toString());

        if (inpFingerprint.getText() != null)
            repoFingerPrint = Util.emptyIfNull(inpFingerprint.getText().toString());


        if (!repoName.isEmpty() || !repoUrl.isEmpty() || !repoFingerPrint.isEmpty()) {
            if (!Util.verifyUrl(repoUrl))
                inpRepoUrl.setError("Invalid URL");
            else {
                final Repo repo = new Repo();
                repo.setRepoId(repoId);
                repo.setRepoName(Util.emptyIfNull(repoName));
                repo.setRepoUrl(repoUrl);
                repo.setRepoFingerprint(repoFingerPrint);

                boolean success = repoListManager.addToRepoMap(repo);
                if (success)
                    dismissAllowingStateLoss();
                else
                    Toast.makeText(requireContext(), "Failed to add repository", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
