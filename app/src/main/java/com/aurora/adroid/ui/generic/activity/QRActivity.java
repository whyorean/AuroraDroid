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

package com.aurora.adroid.ui.generic.activity;

import android.Manifest;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.aurora.adroid.manager.RepoListManager;
import com.aurora.adroid.model.StaticRepo;
import com.aurora.adroid.util.Log;
import com.aurora.adroid.util.Util;
import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class QRActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {
    private ZXingScannerView scannerView;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        checkPermissions();
        scannerView = new ZXingScannerView(this);
        setContentView(scannerView);
    }

    @Override
    public void onResume() {
        super.onResume();
        scannerView.setResultHandler(this);
        scannerView.startCamera();
    }

    @Override
    public void onPause() {
        super.onPause();
        scannerView.stopCamera();
    }

    @Override
    public void handleResult(Result rawResult) {
        scannerView.stopCameraPreview();
        scannerView.stopCamera();
        if (rawResult.getText().contains("fingerprint") || rawResult.getText().contains("FINGERPRINT")) {
            try {
                String[] ss = rawResult.getText().split("\\?");
                StaticRepo staticRepo = new StaticRepo();
                staticRepo.setRepoName(Util.getDomainName(ss[0]));
                staticRepo.setRepoId(String.valueOf(staticRepo.getRepoName().hashCode()));

                ss[0] = ss[0].replace("fdroidrepos", "https");
                ss[1] = ss[1].replace("fingerprint=", "");
                ss[1] = ss[1].replace("FINGERPRINT=", "");

                staticRepo.setRepoUrl(ss[0]);
                staticRepo.setRepoFingerprint(ss[1]);
                new RepoListManager(this).addToRepoMap(staticRepo);
                Toast.makeText(this, "Repo Added Successfully", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, "Failed to add repo", Toast.LENGTH_SHORT).show();
                Log.d(e.getMessage());
            }
        } else {
            Toast.makeText(this, "Unsupported QR", Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    private void checkPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA},
                1337);
    }
}
