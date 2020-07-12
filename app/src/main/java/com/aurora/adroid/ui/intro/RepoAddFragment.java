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

package com.aurora.adroid.ui.intro;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.aurora.adroid.R;
import com.aurora.adroid.manager.RepoListManager;
import com.aurora.adroid.model.StaticRepo;
import com.aurora.adroid.ui.generic.activity.QRActivity;
import com.aurora.adroid.ui.generic.fragment.BaseFragment;
import com.aurora.adroid.util.Util;
import com.google.android.material.textfield.TextInputEditText;

import org.apache.commons.lang3.StringUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RepoAddFragment extends BaseFragment {

    @BindView(R.id.repo_name)
    TextInputEditText inpRepoName;
    @BindView(R.id.repo_url)
    TextInputEditText inpRepoUrl;
    @BindView(R.id.repo_fingerprint)
    TextInputEditText inpFingerprint;

    private NavController navController;
    private RepoListManager repoListManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_repo_add, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        navController = NavHostFragment.findNavController(this);
        repoListManager = new RepoListManager(requireContext());
    }

    @OnClick(R.id.btn_scan_qr)
    public void scanQR() {
        Intent intent = new Intent(requireContext(), QRActivity.class);
        ActivityOptions activityOptions = ActivityOptions.makeSceneTransitionAnimation(requireActivity());
        startActivity(intent, activityOptions.toBundle());
    }

    @OnClick(R.id.save_return)
    public void saveAndReturn() {
        saveRepoToCustomList();
    }

    @OnClick(R.id.action1)
    public void moveBack() {
        navController.navigateUp();
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
                final StaticRepo staticRepo = new StaticRepo();
                staticRepo.setRepoId(repoId);
                staticRepo.setRepoName(Util.emptyIfNull(repoName));
                staticRepo.setRepoUrl(repoUrl);
                staticRepo.setRepoFingerprint(repoFingerPrint);

                boolean success = repoListManager.addToRepoMap(staticRepo);
                if (success)
                    navController.navigateUp();
                else
                    Toast.makeText(requireContext(), "Failed to add repository", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
