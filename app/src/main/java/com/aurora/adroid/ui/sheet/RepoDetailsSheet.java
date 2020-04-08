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

public class RepoDetailsSheet extends BaseBottomSheet {

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
    private RepoListManager repoListManager;
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
        repoListManager = new RepoListManager(requireContext());
        repo = repoListManager.getRepoById(index.getRepoId());

        boolean hasMirror = repo.getRepoMirrors() != null && repo.getRepoMirrors().length >= 1;

        txtName.setText(repo.getRepoName());
        txtUrl.setText(repo.getRepoUrl());

        if (hasMirror) {
            txtMirrorUrl.setText(repo.getRepoMirrors()[0]);
            mirrorSwitch.setChecked(mirrorCheckedList.contains(repo.getRepoId()));
            mirrorSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (mirrorSwitch.isChecked()) {
                    mirrorCheckedList.add(repo.getRepoId());
                    Util.putMirrorCheckedList(requireContext(), mirrorCheckedList);
                } else {
                    mirrorCheckedList.remove(repo.getRepoId());
                    Util.putMirrorCheckedList(requireContext(), mirrorCheckedList);
                }
            });
        } else
            mirrorSwitch.setVisibility(View.GONE);

        txtFingerPrint.setText(repo.getRepoFingerprint());
        txtDescription.setText(repo.getRepoDescription());

        generateQR();
    }

    private void generateQR() {
        QRCodeWriter writer = new QRCodeWriter();
        try {
            String content = repo.getRepoUrl() + "/?fingerprint=" + StringUtils.deleteWhitespace(repo.getRepoFingerprint());
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
}
