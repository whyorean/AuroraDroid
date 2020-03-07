package com.aurora.adroid.ui.sheet;

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

import com.aurora.adroid.R;
import com.aurora.adroid.manager.RepoListManager;
import com.aurora.adroid.model.Index;
import com.aurora.adroid.model.Repo;
import com.aurora.adroid.util.Util;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RepoDetailsBottomSheet extends BaseBottomSheet {

    public static Index index;

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
    private boolean hasMirror;
    private Repo repo;

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
        repo = RepoListManager.getRepoById(requireContext(), index.getRepoId());
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
                Util.putMirrorCheckedList(requireContext(), mirrorCheckedList);
            } else {
                mirrorCheckedList.remove(repo.getRepoId());
                Util.putMirrorCheckedList(requireContext(), mirrorCheckedList);
            }
        });
        generateQR();
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
