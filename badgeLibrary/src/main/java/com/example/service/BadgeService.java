package com.example.service;

import com.example.domain.GithubRepoInformation;
import com.example.domain.RepoInformation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
@Slf4j
public class BadgeService {
    private final GithubService githubService;
    private final RestTemplate restTemplate = new RestTemplate();

    @Cacheable(value = "stars", key = "#repoUrl")
    public int getStars(String repoUrl) throws Exception {
        GithubRepoInformation repoInfo = getRepoInfo(repoUrl);
        return repoInfo != null ? repoInfo.getStars() : 0;
    }

    @Cacheable(value = "language", key = "#repoUrl")
    public String getLanguage(String repoUrl) throws Exception {
        GithubRepoInformation repoInfo = getRepoInfo(repoUrl);
        return repoInfo != null ? repoInfo.getLanguage() : "";
    }

    @Cacheable(value = "issues", key = "#repoUrl")
    public int getIssues(String repoUrl) throws Exception {
        GithubRepoInformation repoInfo = getRepoInfo(repoUrl);
        return repoInfo != null ? repoInfo.getOpenIssues() : 0;
    }

    @Cacheable(value = "forks", key = "#repoUrl")
    public int getForks(String repoUrl) throws Exception {
        GithubRepoInformation repoInfo = getRepoInfo(repoUrl);
        return repoInfo != null ? repoInfo.getForks() : 0;
    }

    @Cacheable(value = "watchers", key = "#repoUrl")
    public int getWatchers(String repoUrl) throws Exception {
        GithubRepoInformation repoInfo = getRepoInfo(repoUrl);
        return repoInfo != null ? repoInfo.getWatchers() : 0;
    }

    @Cacheable(value = "last-commit-date", key = "#repoUrl", unless = "#result == null")
    public String getLastCommitDate(String repoUrl) throws Exception {
        GithubRepoInformation repoInfo = getRepoInfo(repoUrl);
        return repoInfo != null ? repoInfo.getLastCommitDate() : "N/A";
    }

    @Cacheable(value = "created", key = "#repoUrl")
    public String getCreatedAt(String repoUrl) throws Exception {
        GithubRepoInformation repoInfo = getRepoInfo(repoUrl);
        return repoInfo != null ? repoInfo.getCreatedAt() : "N/A";
    }


    @Cacheable(value = "repo-info", key = "#repoUrl")
    public GithubRepoInformation getRepoInfo(String repoUrl) throws Exception {
        RepoInformation parts = ExtractRepoAndOwner.extractOwnerAndRepo(repoUrl);
        String url = String.format("https://api.github.com/repos/%s/%s", parts.getOwner(), parts.getRepo());

        HttpEntity<Void> entity = new HttpEntity<>(new ExtractRepoAndOwner().createHeaders());
        ResponseEntity<GithubRepoInformation> response = restTemplate.exchange(url, HttpMethod.GET, entity, GithubRepoInformation.class);

        GithubRepoInformation repoInfo = response.getBody();
        return repoInfo;
    }

    @Cacheable(value = "user-stats-badge", key = "#username")
    public List<Serializable> getUserStats(String username) {
        log.info("Generating badge for user: {}", username);

        Map<String, Integer> stats = githubService.getUserStats(username);

        int stars = stats.getOrDefault("stars", 0);
        int commits = stats.getOrDefault("commits", 0);
        int prs = stats.getOrDefault("prs", 0);
        int issues = stats.getOrDefault("issues", 0);
        int repositories = stats.getOrDefault("repositories", 0);

        String grade = githubService.calculateGrade(stats);

        log.debug("Stats for {}: Stars={}, Commits={}, PRs={}, Issues={}, Repos={}, Grade={}",
                username, stars, commits, prs, issues, repositories, grade);

        return List.of(username, stars, commits, prs, issues, repositories, grade);

    }

    public String getLatestWorkflowStatus(String repoUrl, String workflow) throws Exception {
        return githubService.getLatestWorkflowStatus(repoUrl, workflow);
    }

    public List<Serializable> getCiStatusBadge(String repoUrl,
                                               String workflow,
                                               String theme) throws Exception {
        String status = this.getLatestWorkflowStatus(repoUrl, workflow);
        String color = switch (status.toLowerCase()) {
            case "success", "completed" -> "#22c55e";
            case "failure", "failed" -> "#ef4444";
            case "in_progress", "queued", "requested" -> "#f59e0b";
            default -> "#6b7280";
        };

        String label = (workflow != null && !workflow.isBlank()) ? ("ci:" + workflow) : "ci";
        return List.of(label, status, color, theme);
    }


}

