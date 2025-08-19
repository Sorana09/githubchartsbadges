package com.example.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GithubRepoInformation {
    @JsonProperty("full_name")
    private String fullName;

    private String description;

    @JsonProperty("stargazers_count")
    private Integer stars = 0;

    @JsonProperty("forks_count")
    private Integer forks = 0;

    @JsonProperty("open_issues_count")
    private Integer openIssues = 0;

    @JsonProperty("watchers_count")
    private Integer watchers = 0;

    private String language;

    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("updated_at")
    private String lastUpdate;

    @JsonProperty("pushed_at")
    private String lastPushed;

    @JsonProperty("html_url")
    private String url;

    private String lastCommitSha;
    private String lastCommitDate;
    private Integer linesAdded = 0;
    private Integer linesDeleted = 0;


}