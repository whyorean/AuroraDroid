package com.aurora.adroid.manager;

import android.content.Context;

import com.aurora.adroid.model.Repo;
import com.aurora.adroid.util.PrefUtil;

import java.util.ArrayList;
import java.util.List;

import static com.aurora.adroid.manager.RepoListManager.getAllRepoList;

public class SyncManager {

    private static final String SYNCED_LIST = "SYNCED_LIST";
    private Context context;

    public SyncManager(Context context) {
        this.context = context;
    }

    public boolean isSynced(String s) {
        ArrayList<String> syncedList = PrefUtil.getListString(context, SYNCED_LIST);
        return syncedList.contains(s);
    }

    public void clearSynced(String s) {
        ArrayList<String> syncedList = PrefUtil.getListString(context, SYNCED_LIST);
        syncedList.remove(s);
        PrefUtil.putListString(context, SYNCED_LIST, syncedList);
    }

    public void clearAllSynced() {
        PrefUtil.putListString(context, SYNCED_LIST, new ArrayList<>());
    }

    public void setSynced(String s) {
        ArrayList<String> syncedList = PrefUtil.getListString(context, SYNCED_LIST);
        syncedList.add(s);
        PrefUtil.putListString(context, SYNCED_LIST, syncedList);
    }

    public List<Repo> getSyncedRepos() {
        List<Repo> repoList = new ArrayList<>();
        List<String> savedList = PrefUtil.getListString(context, SYNCED_LIST);
        for (Repo repo : getAllRepoList(context)) {
            if (savedList.contains(repo.getRepoId()))
                repoList.add(repo);
        }
        return repoList;
    }
}
