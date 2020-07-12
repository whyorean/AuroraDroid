package com.aurora.adroid.ui.generic.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.aurora.adroid.R;
import com.aurora.adroid.ui.intro.IntroActivity;
import com.aurora.adroid.ui.main.AuroraActivity;
import com.aurora.adroid.util.Util;

import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;

public class SplashActivity extends BaseActivity {

    private CompositeDisposable disposable = new CompositeDisposable();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        ButterKnife.bind(this);
        disposable.add(Completable
                .timer(2, TimeUnit.SECONDS, AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    if (Util.isFirstLaunch(this)) {
                        Intent intent = new Intent(this, IntroActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    } else {
                        Intent intent = new Intent(this, AuroraActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }
                    finish();
                }));
    }
}
