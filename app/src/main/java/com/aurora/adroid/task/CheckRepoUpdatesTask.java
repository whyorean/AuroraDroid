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
import android.content.ContextWrapper;

import com.aurora.adroid.AuroraApplication;
import com.aurora.adroid.Constants;
import com.aurora.adroid.R;
import com.aurora.adroid.download.RequestBuilder;
import com.aurora.adroid.event.LogEvent;
import com.aurora.adroid.manager.RepoSyncManager;
import com.aurora.adroid.model.RepoHeader;
import com.aurora.adroid.model.StaticRepo;
import com.aurora.adroid.util.Log;
import com.aurora.adroid.util.Util;
import com.tonyodev.fetch2.Request;
import com.tonyodev.fetch2core.Extras;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.net.ssl.SSLHandshakeException;

import okhttp3.OkHttpClient;
import okhttp3.Response;

public class CheckRepoUpdatesTask extends ContextWrapper {

    private Context context;
    private RepoSyncManager repoSyncManager;

    public CheckRepoUpdatesTask(Context context) {
        super(context);
        this.context = context;
        this.repoSyncManager = new RepoSyncManager(context);
    }

    public List<Request> getRepoRequestList() {

        final RepoSyncManager repoSyncManager = new RepoSyncManager(this);
        final List<StaticRepo> staticRepoList = repoSyncManager.getRepoList();

        if (staticRepoList.isEmpty()) {
            repoSyncManager.addDefault();
            staticRepoList.addAll(repoSyncManager.getRepoList());
        }

        final List<Request> requestList = RequestBuilder.buildRequest(this, staticRepoList);
        final List<Request> filteredList = new ArrayList<>();

        final OkHttpClient client = new OkHttpClient();
        for (Request request : requestList) {
            final Extras extras = request.getExtras();
            final String repoId = extras.getString(Constants.DOWNLOAD_REPO_ID, StringUtils.EMPTY);
            final String repoName = extras.getString(Constants.DOWNLOAD_REPO_NAME, StringUtils.EMPTY);
            final String repoUrl = extras.getString(Constants.DOWNLOAD_REPO_URL, StringUtils.EMPTY);

            if (repoId.isEmpty() || repoName.isEmpty() || repoUrl.isEmpty())
                continue;

            AuroraApplication.rxNotify(new LogEvent("Checking " + repoName + " for updates"));

            final RepoHeader repoHeader = getRepoHeader(extras.getString(Constants.DOWNLOAD_REPO_ID, StringUtils.EMPTY));

            final okhttp3.Request okhttpRequest = new okhttp3.Request.Builder()
                    .url(request.getUrl())
                    .head()
                    .build();

            try (Response response = client.newCall(okhttpRequest).execute()) {
                final String header = response.header("Last-Modified");

                if (header == null) {
                    filteredList.add(request);
                    continue;
                }

                final Long lastModified = Util.getMilliFromDate(header, Calendar.getInstance().getTimeInMillis());
                if (repoHeader.getLastModified() == null) {
                    repoHeader.setRepoId(repoId);
                    repoHeader.setLastModified(lastModified);
                    filteredList.add(request);
                } else {
                    if (repoHeader.getLastModified() < lastModified) {
                        filteredList.add(request);
                    }
                    repoHeader.setLastModified(lastModified);
                }
                repoSyncManager.addToHeaderMap(repoHeader);
            } catch (Exception e) {
                if (e instanceof SSLHandshakeException)
                    AuroraApplication.rxNotify(new LogEvent(StringUtils.joinWith(StringUtils.SPACE, e.getMessage(), "for", repoName)));
                else
                    AuroraApplication.rxNotify(new LogEvent(StringUtils.joinWith(StringUtils.SPACE,
                            context.getString(R.string.repo_unable_to_reach),
                            repoName)));

                Log.e(StringUtils.joinWith(StringUtils.SPACE,
                        context.getString(R.string.repo_unable_to_reach),
                        request.getUrl()));
            }
        }
        return filteredList;
    }

    private RepoHeader getRepoHeader(String repoId) {
        for (RepoHeader repoHeader : repoSyncManager.getHeaderList())
            if (repoHeader.getRepoId().equals(repoId))
                return repoHeader;
        return new RepoHeader();
    }
}
