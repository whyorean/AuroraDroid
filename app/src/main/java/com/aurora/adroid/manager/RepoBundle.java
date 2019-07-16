package com.aurora.adroid.manager;

import com.aurora.adroid.model.Repo;

public class RepoBundle {

    private boolean status;
    private Repo repo;

    public RepoBundle(boolean status, Repo repo) {
        this.status = status;
        this.repo = repo;
    }

    public boolean getStatus() {
        return status;
    }

    public Repo getRepo() {
        return repo;
    }
}
