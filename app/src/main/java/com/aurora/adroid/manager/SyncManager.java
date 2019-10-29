package com.aurora.adroid.manager;

import android.content.Context;

import com.aurora.adroid.Constants;
import com.aurora.adroid.model.Repo;
import com.aurora.adroid.util.PrefUtil;

import java.util.ArrayList;
import java.util.List;

import static com.aurora.adroid.manager.RepoListManager.getAllRepoList;

public class SyncManager {

    private static final String SYNCED_LIST = "SYNCED_LIST";

    public static void clearAllSynced(Context context) {
        PrefUtil.putListString(context, SYNCED_LIST, new ArrayList<>());
    }

    public static void setSynced(Context context, String s) {
        ArrayList<String> syncedList = PrefUtil.getListString(context, SYNCED_LIST);
        syncedList.add(s);
        PrefUtil.putListString(context, SYNCED_LIST, syncedList);
    }

    public static List<Repo> getSyncedRepos(Context context) {
        List<Repo> repoList = new ArrayList<>();
        List<String> savedList = PrefUtil.getListString(context, SYNCED_LIST);
        for (Repo repo : getAllRepoList(context)) {
            if (savedList.contains(repo.getRepoId()))
                repoList.add(repo);
        }
        return repoList;
    }

    public static boolean isSynced(Context context, String s) {
        ArrayList<String> syncedList = PrefUtil.getListString(context, SYNCED_LIST);
        return syncedList.contains(s);
    }

    public static void clearSynced(Context context, String s) {
        ArrayList<String> syncedList = PrefUtil.getListString(context, SYNCED_LIST);
        syncedList.remove(s);
        PrefUtil.putListString(context, SYNCED_LIST, syncedList);
    }

    public static void clearRepoHeader(Context context) {
        PrefUtil.putString(context, Constants.PREFERENCE_REPO_HEADERS, "");
    }
}
