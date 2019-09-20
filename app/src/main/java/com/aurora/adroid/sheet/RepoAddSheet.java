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

package com.aurora.adroid.sheet;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aurora.adroid.R;
import com.aurora.adroid.activity.QRActivity;
import com.aurora.adroid.fragment.RepoListFragment;
import com.aurora.adroid.manager.RepoListManager;
import com.aurora.adroid.model.Repo;
import com.aurora.adroid.util.Util;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RepoAddSheet extends BottomSheetDialogFragment {

    @BindView(R.id.repo_id)
    TextInputEditText inpRepoId;
    @BindView(R.id.repo_name)
    TextInputEditText inpRepoName;
    @BindView(R.id.repo_url)
    TextInputEditText inpRepoUrl;
    @BindView(R.id.repo_fingerprint)
    TextInputEditText inpFingerprint;
    @BindView(R.id.btn_add)
    MaterialButton btnAdd;
    @BindView(R.id.btn_cancel)
    MaterialButton btnCancel;
    @BindView(R.id.img_qr)
    ImageView imgQR;

    public RepoAddSheet() {
    }

    @Override
    public void onStart() {
        super.onStart();
        getDialog().getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sheet_add_repo, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        btnAdd.setOnClickListener(v -> saveRepoToCustomList());
        btnCancel.setOnClickListener(v -> {
            sendResult(false);
            dismissAllowingStateLoss();
        });
        imgQR.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), QRActivity.class);
            startActivity(intent);
            dismissAllowingStateLoss();
        });
    }

    private void saveRepoToCustomList() {
        String repoId = "1337";
        String repoName = "";
        String repoUrl = "";
        String repoFingerPrint = "";

        if (inpRepoId.getText() == null || inpRepoId.getText().toString().isEmpty())
            inpRepoId.setError("Required");
        else
            repoId = Util.emptyIfNull(String.valueOf(inpRepoId.getText().hashCode()));

        if (inpRepoName.getText() == null || inpRepoName.getText().toString().isEmpty())
            inpRepoName.setError("Required");
        else
            repoName = Util.emptyIfNull(inpRepoName.getText().toString());

        if (inpRepoUrl.getText() == null || inpRepoUrl.getText().toString().isEmpty())
            inpRepoUrl.setError("Required");
        else
            repoUrl = Util.emptyIfNull(inpRepoUrl.getText().toString());

        if (inpFingerprint.getText() == null || inpFingerprint.getText().toString().isEmpty())
            inpFingerprint.setHint("Optional");
        else
            repoFingerPrint = Util.emptyIfNull(inpFingerprint.getText().toString());


        if (!repoName.isEmpty() || !repoUrl.isEmpty() || !repoFingerPrint.isEmpty()) {
            if (!Util.verifyUrl(repoUrl))
                inpRepoUrl.setError("Invalid URL");
            else {
                Repo repo = new Repo();
                repo.setRepoId(repoId);
                repo.setRepoName(Util.emptyIfNull(repoName));
                repo.setRepoUrl(repoUrl);
                repo.setRepoFingerprint(repoFingerPrint);

                RepoListManager.addRepoToCustomList(getContext(), repo);
                sendResult(true);
                dismissAllowingStateLoss();
            }
        }
    }

    private void sendResult(Boolean added) {
        if (getTargetFragment() == null) {
            return;
        }
        Intent intent = RepoListFragment.newIntent(added);
        getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
        dismiss();
    }
}
