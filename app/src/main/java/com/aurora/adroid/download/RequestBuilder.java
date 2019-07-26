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
import com.aurora.adroid.model.Repo;
import com.aurora.adroid.util.PathUtil;
import com.aurora.adroid.util.Util;
import com.tonyodev.fetch2.EnqueueAction;
import com.tonyodev.fetch2.NetworkType;
import com.tonyodev.fetch2.Request;

import java.util.ArrayList;
import java.util.List;

import static com.aurora.adroid.Constants.SIGNED_FILE_NAME;

public class RequestBuilder {

    public static List<Request> buildRequest(Context context, List<Repo> repoList) {
        List<Request> requestList = new ArrayList<>();
        for (Repo repo : repoList) {
            /*if (RepoListManager.isSynced(context, repo.getRepoId()))
                continue;*/
            String Url = repo.getRepoUrl();
            if (Util.isMirrorChecked(context, repo.getRepoId()) && repo.getRepoMirrors() != null)
                Url = repo.getRepoMirrors()[0];
            final Request request = new Request(Url + "/" + SIGNED_FILE_NAME,
                    PathUtil.getRepoDirectory(context) + repo.getRepoId() + "." + Constants.JAR);
            request.setGroupId(1337);
            request.setEnqueueAction(EnqueueAction.REPLACE_EXISTING);
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
