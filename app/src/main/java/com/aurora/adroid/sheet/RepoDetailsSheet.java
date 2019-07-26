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

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;

import com.aurora.adroid.R;
import com.aurora.adroid.model.Repo;
import com.aurora.adroid.util.Util;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RepoDetailsSheet extends BottomSheetDialogFragment {

    @BindView(R.id.img_share)
    ImageView imgShare;
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

    private Context context;
    private Repo repo;
    private ArrayList<String> mirrorCheckedList = new ArrayList<>();
    private boolean hasMirror;

    public RepoDetailsSheet() {
    }

    public void setRepo(Repo repo) {
        this.repo = repo;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sheet_repo_details, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mirrorCheckedList = Util.getMirrorCheckedList(context);
        hasMirror = repo.getRepoMirrors() != null && repo.getRepoMirrors().length >= 1;
        txtName.setText(repo.getRepoName());
        txtUrl.setText(repo.getRepoUrl());
        if (hasMirror)
            txtMirrorUrl.setText(repo.getRepoMirrors()[0]);
        txtFingerPrint.setText(repo.getRepoFingerprint());
        txtDescription.setText(repo.getRepoDescription());

        if (!hasMirror)
            mirrorSwitch.setVisibility(View.GONE);

        if (mirrorCheckedList.contains(repo.getRepoId()))
            mirrorSwitch.setChecked(true);

        mirrorSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (mirrorSwitch.isChecked()) {
                mirrorCheckedList.add(repo.getRepoId());
                Util.putMirrorCheckedList(context, mirrorCheckedList);
            } else {
                mirrorCheckedList.remove(repo.getRepoId());
                Util.putMirrorCheckedList(context, mirrorCheckedList);
            }
        });
        setupShare();
        generateQR();
    }

    private void setupShare() {
        imgShare.setOnClickListener(v -> {
            String fingerprint = repo.getRepoFingerprint();
            fingerprint = StringUtils.deleteWhitespace(fingerprint);
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("text/plain");
            i.putExtra(Intent.EXTRA_SUBJECT, repo.getRepoName());
            i.putExtra(Intent.EXTRA_TEXT, repo.getRepoUrl()
                    + (!TextUtils.isEmpty(fingerprint) ? "/?fingerprint=" + fingerprint : ""));
            context.startActivity(Intent.createChooser(i, getString(R.string.action_share)));
            dismissAllowingStateLoss();
        });
    }

    private void generateQR() {
        QRCodeWriter writer = new QRCodeWriter();
        try {
            StringBuilder content = new StringBuilder()
                    .append(repo.getRepoUrl())
                    .append("/?fingerprint=")
                    .append(StringUtils.deleteWhitespace(repo.getRepoFingerprint()));
            BitMatrix bitMatrix = writer.encode(content.toString(), BarcodeFormat.QR_CODE, 512, 512);
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bmp.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            imgQR.setImageBitmap(bmp);
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }
}
