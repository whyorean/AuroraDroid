package com.aurora.adroid.model;

import android.net.Uri;

import com.tonyodev.fetch2.Request;

import org.jetbrains.annotations.NotNull;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RepoRequest extends Request {

    private String repoId;
    private String repoName;
    private String repoUrl;

    public RepoRequest(@NotNull String url, @NotNull String file) {
        super(url, file);
    }

    public RepoRequest(@NotNull String url, @NotNull Uri fileUri) {
        super(url, fileUri);
    }
}
