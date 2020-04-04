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

package com.aurora.adroid.download;

import android.content.Context;

import com.aurora.adroid.Constants;
import com.aurora.adroid.model.App;
import com.aurora.adroid.model.Package;
import com.aurora.adroid.model.Repo;
import com.aurora.adroid.model.RepoRequest;
import com.aurora.adroid.util.DatabaseUtil;
import com.aurora.adroid.util.PathUtil;
import com.aurora.adroid.util.Util;
import com.tonyodev.fetch2.EnqueueAction;
import com.tonyodev.fetch2.NetworkType;
import com.tonyodev.fetch2.Request;
import com.tonyodev.fetch2core.Extras;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.aurora.adroid.Constants.SIGNED_FILE_NAME;

public class RequestBuilder {

    public static Request buildRequest(Context context, App app) {
        final Request request = new Request(DatabaseUtil.getDownloadURl(app),
                PathUtil.getApkPath(context, app.getAppPackage().getApkName()));
        addAppExtras(request, app);
        request.setEnqueueAction(EnqueueAction.REPLACE_EXISTING);
        request.setGroupId(app.getPackageName().hashCode());
        request.setTag(app.getPackageName());
        return request;
    }

    public static Request buildRequest(Context context, Package pkg, App app) {
        final Request request = new Request(DatabaseUtil.getDownloadURl(pkg),
                PathUtil.getApkPath(context, pkg.getApkName()));
        addAppExtras(request, app);
        request.setEnqueueAction(EnqueueAction.REPLACE_EXISTING);
        request.setGroupId(app.getPackageName().hashCode());
        request.setTag(app.getPackageName());
        return request;
    }

    public static List<Request> buildRequest(Context context, List<Repo> repoList) {
        final List<Request> requestList = new ArrayList<>();
        for (Repo repo : repoList) {

            String repoUrl = repo.getRepoUrl();

            if (Util.isMirrorChecked(context, repo.getRepoId()) && repo.getRepoMirrors() != null)
                repoUrl = repo.getRepoMirrors()[0];

            final RepoRequest request = new RepoRequest(repoUrl + "/" + SIGNED_FILE_NAME,
                    PathUtil.getRepoDirectory(context) + repo.getRepoId() + "." + Constants.JAR);
            addRepoExtras(request, repo);

            if (Util.isDownloadWifiOnly(context))
                request.setNetworkType(NetworkType.WIFI_ONLY);
            else
                request.setNetworkType(NetworkType.GLOBAL_OFF);

            requestList.add(request);
        }
        return requestList;
    }

    private static void addAppExtras(Request request, App app) {
        final Map<String, String> stringMap = new HashMap<>();
        stringMap.put(Constants.DOWNLOAD_PACKAGE_NAME, app.getPackageName());
        stringMap.put(Constants.DOWNLOAD_DISPLAY_NAME, app.getName());
        stringMap.put(Constants.DOWNLOAD_VERSION_NAME, app.getSuggestedVersionName());
        stringMap.put(Constants.DOWNLOAD_VERSION_CODE, String.valueOf(app.getSuggestedVersionCode()));
        stringMap.put(Constants.DOWNLOAD_ICON_URL, DatabaseUtil.getImageUrl(app));

        final Extras extras = new Extras(stringMap);
        request.setExtras(extras);
    }

    private static void addRepoExtras(Request request, Repo repo) {
        final Map<String, String> stringMap = new HashMap<>();
        stringMap.put(Constants.DOWNLOAD_REPO_ID, repo.getRepoId());
        stringMap.put(Constants.DOWNLOAD_REPO_NAME, repo.getRepoName());
        stringMap.put(Constants.DOWNLOAD_REPO_FINGERPRINT, repo.getRepoFingerprint());
        stringMap.put(Constants.DOWNLOAD_REPO_URL, repo.getRepoUrl());

        final Extras extras = new Extras(stringMap);
        request.setExtras(extras);
        request.setTag(repo.getRepoId());
        request.setGroupId(1337);
    }
}
