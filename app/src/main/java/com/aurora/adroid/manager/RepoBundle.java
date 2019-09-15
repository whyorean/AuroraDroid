package com.aurora.adroid.manager;

import com.aurora.adroid.model.Repo;

public class RepoBundle {

    private boolean synced;
    private Repo repo;

    public RepoBundle(boolean status, Repo repo) {
        this.synced = status;
        this.repo = repo;
    }

    public boolean isSynced() {
        return synced;
    }

    public Repo getRepo() {
        return repo;
    }
}
