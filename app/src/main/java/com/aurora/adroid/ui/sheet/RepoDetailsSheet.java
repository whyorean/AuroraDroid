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

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;

import com.aurora.adroid.Constants;
import com.aurora.adroid.R;
import com.aurora.adroid.database.AppDatabase;
import com.aurora.adroid.database.RepoDao;
import com.aurora.adroid.manager.RepoListManager;
import com.aurora.adroid.model.StaticRepo;
import com.aurora.adroid.util.Util;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class RepoDetailsSheet extends BaseBottomSheet {

    public static final String TAG = "REPO_DETAIL_SHEET";

    @BindView(R.id.img_qr)
    ImageView imgQR;
    @BindView(R.id.txt_name)
    TextView txtName;
    @BindView(R.id.txt_url)
    TextView txtUrl;
    @BindView(R.id.txt_fingerprint)
    TextView txtFingerPrint;
    @BindView(R.id.txt_description)
    TextView txtDescription;
    @BindView(R.id.switch_mirror)
    SwitchCompat mirrorSwitch;
    @BindView(R.id.txt_mirror_url)
    TextView txtMirrorUrl;

    private ArrayList<String> mirrorCheckedList = new ArrayList<>();
    private RepoListManager repoListManager;
    private StaticRepo staticRepo;

    private CompositeDisposable disposable = new CompositeDisposable();

    @Nullable
    @Override
    protected View onCreateContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_repo_details, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    protected void onContentViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mirrorCheckedList = Util.getMirrorCheckedList(requireContext());
        repoListManager = new RepoListManager(requireContext());

        Bundle bundle = getArguments();
        if (bundle != null) {
            String repoId = bundle.getString(Constants.STRING_EXTRA, "1");
            staticRepo = repoListManager.getRepoById(repoId);
            populate(staticRepo);
        } else {
            dismissAllowingStateLoss();
        }
    }

    @OnClick(R.id.btn_share)
    public void shareIt() {
        generateShareIntent();
    }

    private void populate(StaticRepo staticRepo) {
        boolean hasMirror = staticRepo.getRepoMirrors() != null && staticRepo.getRepoMirrors().length >= 1;

        txtName.setText(staticRepo.getRepoName());
        txtUrl.setText(staticRepo.getRepoUrl());

        if (hasMirror) {
            txtMirrorUrl.setText(staticRepo.getRepoMirrors()[0]);
            mirrorSwitch.setChecked(mirrorCheckedList.contains(staticRepo.getRepoId()));
            mirrorSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (mirrorSwitch.isChecked()) {
                    mirrorCheckedList.add(staticRepo.getRepoId());
                } else {
                    mirrorCheckedList.remove(staticRepo.getRepoId());
                }
                Util.putMirrorCheckedList(requireContext(), mirrorCheckedList);
            });
        } else
            mirrorSwitch.setVisibility(View.GONE);

        txtFingerPrint.setText(staticRepo.getRepoFingerprint());

        if (StringUtils.isNotEmpty(staticRepo.getRepoDescription()))
            txtDescription.setText(staticRepo.getRepoDescription());
        else
            fetchRepoFromDatabase();

        generateQR();
    }

    private void fetchRepoFromDatabase() {
        AppDatabase appDatabase = AppDatabase.getDatabase(requireContext());
        RepoDao repoDao = appDatabase.repoDao();
        disposable.add(Observable.fromCallable(() -> repoDao.getRepoByRepoId(staticRepo.getRepoId()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(repo -> {
                    if (repo != null) {
                        txtDescription.setText(repo.getDescription());
                    }
                }));
    }

    private void generateQR() {
        QRCodeWriter writer = new QRCodeWriter();
        try {
            String content = staticRepo.getRepoUrl() + "/?fingerprint=" + StringUtils.deleteWhitespace(staticRepo.getRepoFingerprint());
            final BitMatrix bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, 512, 512);
            final int width = bitMatrix.getWidth();
            final int height = bitMatrix.getHeight();
            final Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            imgQR.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }

    private void generateShareIntent() {
        String fingerprint = staticRepo.getRepoFingerprint();
        fingerprint = StringUtils.deleteWhitespace(fingerprint);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, staticRepo.getRepoName());
        intent.putExtra(Intent.EXTRA_TEXT, staticRepo.getRepoUrl()
                + (StringUtils.isNotEmpty(fingerprint) ? "/?fingerprint=" + fingerprint : ""));
        startActivity(Intent.createChooser(intent, getString(R.string.action_share)));
    }
}
