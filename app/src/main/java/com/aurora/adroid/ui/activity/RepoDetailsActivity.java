package com.aurora.adroid.ui.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;

import com.aurora.adroid.R;
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

public class RepoDetailsActivity extends AppCompatActivity {

    public static Repo repo;
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
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    private ActionBar actionBar;
    private ArrayList<String> mirrorCheckedList = new ArrayList<>();
    private boolean hasMirror;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_repo_details);
        ButterKnife.bind(this);
        setupActionbar();

        mirrorCheckedList = Util.getMirrorCheckedList(this);
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
                Util.putMirrorCheckedList(this, mirrorCheckedList);
            } else {
                mirrorCheckedList.remove(repo.getRepoId());
                Util.putMirrorCheckedList(this, mirrorCheckedList);
            }
        });
        generateQR();
    }

    private void setupActionbar() {
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setElevation(0f);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_repo_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.action_share:
                String fingerprint = repo.getRepoFingerprint();
                fingerprint = StringUtils.deleteWhitespace(fingerprint);
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_SUBJECT, repo.getRepoName());
                i.putExtra(Intent.EXTRA_TEXT, repo.getRepoUrl()
                        + (!TextUtils.isEmpty(fingerprint) ? "/?fingerprint=" + fingerprint : ""));
                startActivity(Intent.createChooser(i, getString(R.string.action_share)));
                break;
        }
        return true;
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
