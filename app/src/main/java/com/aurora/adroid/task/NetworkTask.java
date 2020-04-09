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

package com.aurora.adroid.task;

import android.content.Context;

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

    public String getRawResponse(String url) throws Exception {
        final OkHttpClient client = getOkHttpClient(context);
        final Request request = new Request.Builder().url(url).build();
        final Response response = client.newCall(request).execute();
        return response.body().string();
    }

    public int getStatus(String url) throws Exception {
        final OkHttpClient client = getOkHttpClient(context);
        final Request request = new Request.Builder().url(url).build();
        final Response response = client.newCall(request).execute();
        return response.code();
    }
}
