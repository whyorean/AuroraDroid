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

import com.aurora.adroid.manager.RepoListManager;
import com.aurora.adroid.model.Repo;
import com.aurora.adroid.util.PathUtil;
import com.aurora.adroid.util.Util;
import com.tonyodev.fetch2.NetworkType;
import com.tonyodev.fetch2.Request;

import java.util.ArrayList;
import java.util.List;

public class RequestBuilder {

    public static final String EXT_JAR = ".jar";
    public static final String REPO_FILE = "/index-v1" + EXT_JAR;

    public static List<Request> buildRequest(Context context, List<Repo> repoList) {
        List<Request> requestList = new ArrayList<>();
        for (Repo repo : repoList) {
            if (RepoListManager.isSynced(context, repo.getRepoId()))
                continue;
            final Request request = new Request(repo.getRepoUrl() + REPO_FILE, PathUtil.getRepoDirectory(context) + repo.getRepoId() + EXT_JAR);
            request.setGroupId(1337);
            request.setTag(repo.getRepoId());
            if (Util.isDownloadWifiOnly(context))
                request.setNetworkType(NetworkType.WIFI_ONLY);
            else
                request.setNetworkType(NetworkType.GLOBAL_OFF);
            requestList.add(request);
        }
        return requestList;
    }
}
