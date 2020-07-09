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

package com.aurora.adroid.download;

import android.content.Context;

import com.aurora.adroid.Constants;
import com.aurora.adroid.model.App;
import com.aurora.adroid.model.Package;
import com.aurora.adroid.model.StaticRepo;
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
                PathUtil.getApkPath(context, app.getPackageName(), app.getPkg().getVersionCode()));
        addAppExtras(request, app, null);
        request.setEnqueueAction(EnqueueAction.REPLACE_EXISTING);
        request.setGroupId(app.getPackageName().hashCode());
        request.setTag(app.getPackageName());
        return request;
    }

    public static Request buildRequest(Context context, App app, Package pkg) {
        final Request request = new Request(DatabaseUtil.getDownloadURl(app, pkg),
                PathUtil.getApkPath(context, pkg.getPackageName(), pkg.getVersionCode()));
        addAppExtras(request, app, pkg);
        request.setEnqueueAction(EnqueueAction.REPLACE_EXISTING);
        request.setGroupId(app.getPackageName().hashCode());
        request.setTag(app.getPackageName());
        return request;
    }

    public static List<Request> buildRequest(Context context, List<StaticRepo> staticRepoList) {
        final List<Request> requestList = new ArrayList<>();
        for (StaticRepo staticRepo : staticRepoList) {

            String repoUrl = staticRepo.getRepoUrl();

            if (Util.isMirrorChecked(context, staticRepo.getRepoId()) && staticRepo.getRepoMirrors() != null)
                repoUrl = staticRepo.getRepoMirrors()[0];

            final RepoRequest request = new RepoRequest(repoUrl + "/" + SIGNED_FILE_NAME,
                    PathUtil.getRepoDirectory(context) + staticRepo.getRepoId() + "." + Constants.JAR);
            addRepoExtras(request, staticRepo);

            if (Util.isDownloadWifiOnly(context))
                request.setNetworkType(NetworkType.WIFI_ONLY);
            else
                request.setNetworkType(NetworkType.GLOBAL_OFF);

            requestList.add(request);
        }
        return requestList;
    }

    private static void addAppExtras(Request request, App app, Package pkg) {
        final Map<String, String> stringMap = new HashMap<>();
        stringMap.put(Constants.DOWNLOAD_PACKAGE_NAME, app.getPackageName());
        stringMap.put(Constants.DOWNLOAD_DISPLAY_NAME, app.getName());
        stringMap.put(Constants.DOWNLOAD_VERSION_NAME, app.getPkg().getVersionName());
        stringMap.put(Constants.DOWNLOAD_VERSION_CODE, String.valueOf(pkg == null
                ? app.getPkg().getVersionCode()
                : pkg.getVersionCode()));
        stringMap.put(Constants.DOWNLOAD_ICON_URL, DatabaseUtil.getImageUrl(app));
        stringMap.put(Constants.DOWNLOAD_APK_NAME, pkg == null
                ? app.getPkg().getApkName()
                : pkg.getApkName());

        final Extras extras = new Extras(stringMap);
        request.setExtras(extras);
    }

    private static void addRepoExtras(Request request, StaticRepo staticRepo) {
        final Map<String, String> stringMap = new HashMap<>();
        stringMap.put(Constants.DOWNLOAD_REPO_ID, staticRepo.getRepoId());
        stringMap.put(Constants.DOWNLOAD_REPO_NAME, staticRepo.getRepoName());
        stringMap.put(Constants.DOWNLOAD_REPO_FINGERPRINT, staticRepo.getRepoFingerprint());
        stringMap.put(Constants.DOWNLOAD_REPO_URL, staticRepo.getRepoUrl());

        final Extras extras = new Extras(stringMap);
        request.setExtras(extras);
        request.setTag(staticRepo.getRepoId());
        request.setGroupId(1337);
    }
}
