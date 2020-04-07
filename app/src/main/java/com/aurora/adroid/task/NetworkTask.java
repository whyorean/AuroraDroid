package com.aurora.adroid.task;

import android.content.Context;

import com.aurora.adroid.util.Log;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class NetworkTask {

    private Context context;

    public NetworkTask(Context context) {
        this.context = context;
    }

    private static OkHttpClient getOkHttpClient(Context context) {
        final OkHttpClient.Builder builder = new OkHttpClient.Builder();
        return builder.build();
    }

    public String get(String url) throws Exception {
        Log.e(url);
        final OkHttpClient client = getOkHttpClient(context);
        final Request request = new Request.Builder().url(url).build();
        final Response response = client.newCall(request).execute();
        return response.body().string();
    }
}
