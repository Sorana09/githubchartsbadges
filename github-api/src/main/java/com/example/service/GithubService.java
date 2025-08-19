package com.example.service;

import com.example.domain.Commit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.time.ZonedDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class GithubService {

    private final RestTemplate restTemplate;
    private final ExtractRepoAndOwner extractRepoAndOwner;

    @Value("${github.api.token:}")
    private String githubToken;

    public Map<Integer, Integer> getCommitsPerYearPerProject(String repoUrl) throws Exception {
        if (repoUrl == null || !repoUrl.contains("github.com")) {
            throw new IllegalArgumentException("Invalid GitHub repository URL");
        }

        String[] parts = new URI(repoUrl).getPath().split("/");
        if (parts.length < 3) throw new IllegalArgumentException("Could not extract owner/repo");

        String owner = parts[1];
        String repo = parts[2].replaceAll("\\.git$", "");

        Map<Integer, Integer> commitsPerYear = new TreeMap<>();

        int page = 1;

        while (true) {
            String url = String.format(
                    "https://api.github.com/repos/%s/%s/commits?per_page=%d&page=%d",
                    owner, repo, ExtractRepoAndOwner.PER_PAGE, page
            );

            HttpEntity<Void> entity = new HttpEntity<>(extractRepoAndOwner.createHeaders());
            ResponseEntity<Commit[]> response = restTemplate.exchange(url, HttpMethod.GET, entity, Commit[].class);

            Commit[] commits = response.getBody();
            if (commits == null || commits.length == 0) {
                break;
            }

            for (Commit c : commits) {
                try {
                    String dateStr = c.getCommit().getAuthor().getDate();
                    if (dateStr != null && !dateStr.isBlank()) {
                        ZonedDateTime zdt = ZonedDateTime.parse(dateStr);
                        int year = zdt.getYear();
                        commitsPerYear.put(year, commitsPerYear.getOrDefault(year, 0) + 1);
                    }
                } catch (Exception e) {
                    log.warn("Skipping invalid date: {}", e.getMessage());
                }
            }

            if (commits.length < ExtractRepoAndOwner.PER_PAGE) {
                break;
            } else page++;
        }

        return commitsPerYear;
    }

    public Map<String, Integer> getUserStats(String username) {
        Map<String, Integer> stats = new HashMap<>();

        try {
            log.info("Fetching GitHub stats for user: {}", username);

            Map<String, Object> userInfo = getUserInfo(username);
            int publicRepos = ((Number) userInfo.getOrDefault("public_repos", 0)).intValue();

            log.info("User {} has {} public repositories", username, publicRepos);

            List<Map<String, Object>> allRepos = fetchAllUserRepos(username);

            int totalStars = allRepos.stream()
                    .mapToInt(repo -> ((Number) repo.getOrDefault("stargazers_count", 0)).intValue())
                    .sum();
            int[] contributionData = getContributionStats(username);
            int totalCommits = contributionData[0];
            int totalPRs = contributionData[1];
            int totalIssues = contributionData[2];

            int totalContributions = totalCommits + totalPRs + totalIssues;

            stats.put("stars", totalStars);
            stats.put("commits", totalCommits);
            stats.put("prs", totalPRs);
            stats.put("issues", totalIssues);
            stats.put("contributions", totalContributions);
            stats.put("repositories", publicRepos);

            log.info("Stats for {}: Stars={}, Commits={}, PRs={}, Issues={}, Total Contributions={}",
                    username, totalStars, totalCommits, totalPRs, totalIssues, totalContributions);

        } catch (Exception e) {
            log.error("Error fetching stats for user {}: {}", username, e.getMessage());
            stats.putAll(getDefaultStats());
        }

        return stats;
    }

    private Map<String, Object> getUserInfo(String username) {
        String url = "https://api.github.com/users/" + username;

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(extractRepoAndOwner.createHeaders()),
                    Map.class
            );

            return (Map<String, Object>) response.getBody();
        } catch (HttpClientErrorException e) {
            log.warn("Failed to fetch user info for {}: {}", username, e.getMessage());
            return new HashMap<>();
        }
    }

    private List<Map<String, Object>> fetchAllUserRepos(String username) {
        List<Map<String, Object>> allRepos = new ArrayList<>();
        int page = 1;
        int perPage = 100;

        while (true) {
            String url = String.format(
                    "https://api.github.com/users/%s/repos?type=all&sort=updated&per_page=%d&page=%d",
                    username, perPage, page
            );

            try {
                ResponseEntity<Map[]> response = restTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        new HttpEntity<>(extractRepoAndOwner.createHeaders()),
                        Map[].class
                );

                Map[] repos = response.getBody();
                if (repos == null || repos.length == 0) {
                    break;
                }

                allRepos.addAll(Arrays.asList((Map<String, Object>[]) repos));

                if (repos.length < perPage) {
                    break;
                }

                page++;

                if (page > 10) {
                    log.warn("Limiting repo fetch to 1000 repos for user {}", username);
                    break;
                }

            } catch (HttpClientErrorException e) {
                log.warn("Failed to fetch repos for user {} at page {}: {}", username, page, e.getMessage());
                break;
            }
        }

        log.info("Fetched {} repositories for user {}", allRepos.size(), username);
        return allRepos;
    }

    private int[] getContributionStats(String username) {
        int totalCommits = getCommitCount(username);
        int totalPRs = getSearchCount(username, "is:pr author:" + username);
        int totalIssues = getSearchCount(username, "is:issue author:" + username);

        return new int[]{totalCommits, totalPRs, totalIssues};
    }

    private int getCommitCount(String username) {
        String query = "author:" + username;
        return getSearchCount(username, query, "commits");
    }

    private int getSearchCount(String username, String query) {
        return getSearchCount(username, query, "issues");
    }

    private int getSearchCount(String username, String query, String searchType) {
        String baseUrl = searchType.equals("commits")
                ? "https://api.github.com/search/commits"
                : "https://api.github.com/search/issues";

        String url = String.format("%s?q=%s&per_page=1", baseUrl, query);

        try {
            HttpHeaders headers = extractRepoAndOwner.createHeaders();
            if (searchType.equals("commits")) {
                headers.add("Accept", "application/vnd.github.cloak-preview+json");
            }

            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    Map.class
            );

            Map body = response.getBody();
            if (body != null && body.containsKey("total_count")) {
                int totalCount = ((Number) body.get("total_count")).intValue();
                return Math.min(totalCount, 1000);
            }
        } catch (HttpClientErrorException e) {
            log.warn("Failed to get {} count for user {}: {}", searchType, username, e.getMessage());
            if (e.getStatusCode().value() == 403) {
                log.warn("Rate limit exceeded for user {}", username);
            }
        } catch (Exception e) {
            log.error("Unexpected error getting {} count for user {}: {}", searchType, username, e.getMessage());
        }

        return 0;
    }

    private Map<String, Integer> getDefaultStats() {
        Map<String, Integer> defaultStats = new HashMap<>();
        defaultStats.put("stars", 0);
        defaultStats.put("commits", 0);
        defaultStats.put("prs", 0);
        defaultStats.put("issues", 0);
        defaultStats.put("contributions", 0);
        defaultStats.put("repositories", 0);
        return defaultStats;
    }

    public String calculateGrade(Map<String, Integer> stats) {
        int stars = stats.getOrDefault("stars", 0);
        int commits = stats.getOrDefault("commits", 0);
        int prs = stats.getOrDefault("prs", 0);
        int issues = stats.getOrDefault("issues", 0);
        int repositories = stats.getOrDefault("repositories", 0);

        double score = (stars * 2.0) + (commits * 1.0) + (prs * 3.0) + (issues * 1.5) + (repositories * 5.0);

        if (score >= 1000) return "A+";
        if (score >= 750) return "A";
        if (score >= 500) return "B+";
        if (score >= 250) return "B";
        if (score >= 100) return "C+";
        if (score >= 50) return "C";
        if (score >= 25) return "D";
        return "F";
    }
}