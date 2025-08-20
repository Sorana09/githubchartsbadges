package com.example.service;

import com.example.domain.RepoInformation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;


@Component
public class ExtractRepoAndOwner {


    public static final int PER_PAGE = 100;

    @Value("${github.token:}")
    public String githubToken;


    public static RepoInformation extractOwnerAndRepo(String repoUrl) throws Exception {
        if (repoUrl == null || !repoUrl.contains("github.com")) {
            throw new IllegalArgumentException("Invalid GitHub repository URL");
        }

        String[] parts = new java.net.URI(repoUrl).getPath().split("/");
        if (parts.length < 3) throw new IllegalArgumentException("Could not extract owner/repo");

        String owner = parts[1];
        String repo = parts[2].replaceAll("\\.git$", "");

        return new RepoInformation(owner, repo);
    }

    public HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, "application/vnd.github+json");
        headers.set(HttpHeaders.USER_AGENT, "GitHub-Monitoring-App");
        if (githubToken != null && !githubToken.isBlank()) {
            headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + githubToken);
        }
        return headers;
    }

}
