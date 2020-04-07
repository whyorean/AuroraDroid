package com.aurora.adroid.task;

import android.content.Context;
import android.content.ContextWrapper;

import com.aurora.adroid.AuroraApplication;
import com.aurora.adroid.Constants;
import com.aurora.adroid.download.RequestBuilder;
import com.aurora.adroid.event.LogEvent;
import com.aurora.adroid.manager.RepoSyncManager;
import com.aurora.adroid.model.Repo;
import com.aurora.adroid.model.RepoHeader;
import com.aurora.adroid.util.Log;
import com.aurora.adroid.util.Util;
import com.tonyodev.fetch2.Request;
import com.tonyodev.fetch2core.Extras;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

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
        final List<Repo> repoList = repoSyncManager.getRepoList();
        final List<Request> requestList = RequestBuilder.buildRequest(this, repoList);
        final List<Request> filteredList = new ArrayList<>();

        if (repoList.isEmpty()) {
            return new ArrayList<>();
        }

        final OkHttpClient client = new OkHttpClient();
        for (Request request : requestList) {
            final Extras extras = request.getExtras();
            final String repoId = extras.getString(Constants.DOWNLOAD_REPO_ID, StringUtils.EMPTY);
            final String repoName = extras.getString(Constants.DOWNLOAD_REPO_NAME, StringUtils.EMPTY);
            final String repoUrl = extras.getString(Constants.DOWNLOAD_REPO_URL, StringUtils.EMPTY);

            if (repoId.isEmpty() || repoName.isEmpty() || repoUrl.isEmpty())
                continue;

            AuroraApplication.rxNotify(new LogEvent("Checking update for " + repoName));

            final RepoHeader repoHeader = getRepoHeader(extras.getString(Constants.DOWNLOAD_REPO_ID, StringUtils.EMPTY));
            final okhttp3.Request okhttpRequest = new okhttp3.Request.Builder().url(request.getUrl()).head().build();

            try (Response response = client.newCall(okhttpRequest).execute()) {
                final Long lastModified = Util.getMilliFromDate(response.header("Last-Modified"),
                        Calendar.getInstance().getTimeInMillis());
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
                AuroraApplication.rxNotify(new LogEvent("Unable to reach " + repoName));
                Log.e("Unable to reach %s", repoUrl);
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
