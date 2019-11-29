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

package com.aurora.adroid.activity;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.aurora.adroid.R;
import com.aurora.adroid.activity.QRActivity;
import com.aurora.adroid.manager.RepoListManager;
import com.aurora.adroid.model.Repo;
import com.aurora.adroid.util.Util;
import com.google.android.material.textfield.TextInputEditText;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RepoAddActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.repo_id)
    TextInputEditText inpRepoId;
    @BindView(R.id.repo_name)
    TextInputEditText inpRepoName;
    @BindView(R.id.repo_url)
    TextInputEditText inpRepoUrl;
    @BindView(R.id.repo_fingerprint)
    TextInputEditText inpFingerprint;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_repo_add);
        ButterKnife.bind(this);
        setupActionbar();
    }

    @OnClick(R.id.btn_add)
    public void addRepo() {
        saveRepoToCustomList();
        finish();
    }

    @OnClick(R.id.btn_cancel)
    public void cancelRepo() {
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_repo_add, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.action_scan:
                Intent intent = new Intent(this, QRActivity.class);
                ActivityOptions activityOptions = ActivityOptions.makeSceneTransitionAnimation(this);
                startActivity(intent, activityOptions.toBundle());
                finish();
                break;
        }
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void setupActionbar() {
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setElevation(0f);
            actionBar.setTitle(getString(R.string.title_repo_add));
        }
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

        if (inpFingerprint.getText() != null)
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

                RepoListManager.addRepoToCustomList(this, repo);
                finish();
            }
        }
    }
}
