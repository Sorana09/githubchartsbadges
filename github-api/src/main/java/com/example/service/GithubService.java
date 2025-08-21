package com.example.service;

import com.example.domain.Commit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GithubService {

    private final RestTemplate restTemplate;
    private final ExtractRepoAndOwner extractRepoAndOwner;

    @Value("${github.api.token:}")
    private String githubToken;


    private String[] extractOwnerRepo(String repoUrl) throws Exception {
        if (repoUrl == null || !repoUrl.contains("github.com")) {
            throw new IllegalArgumentException("Invalid GitHub repository URL");
        }
        String[] parts = new URI(repoUrl).getPath().split("/");
        if (parts.length < 3) throw new IllegalArgumentException("Could not extract owner/repo");
        return new String[]{parts[1], parts[2].replaceAll("\\.git$", "")};
    }

    private <T> T exchange(String url, Class<T> type) {
        try {
            ResponseEntity<T> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(extractRepoAndOwner.createHeaders()),
                    type
            );
            return response.getBody();
        } catch (HttpClientErrorException e) {
            log.warn("GitHub API call failed: {} -> {}", url, e.getMessage());
            return null;
        }
    }

    private ZonedDateTime parseDate(String dateStr) {
        try {
            return (dateStr != null && !dateStr.isBlank()) ? ZonedDateTime.parse(dateStr) : null;
        } catch (Exception e) {
            log.debug("Invalid date format: {}", dateStr);
            return null;
        }
    }


    @Cacheable(value = "github:commits-yearly", key = "#repoUrl")
    public Map<Integer, Integer> getCommitsPerYearPerProject(String repoUrl) throws Exception {
        String[] ownerRepo = extractOwnerRepo(repoUrl);
        String owner = ownerRepo[0], repo = ownerRepo[1];

        Map<Integer, Integer> commitsPerYear = new TreeMap<>();
        int page = 1;

        while (true) {
            String url = String.format(
                    "https://api.github.com/repos/%s/%s/commits?per_page=%d&page=%d",
                    owner, repo, ExtractRepoAndOwner.PER_PAGE, page
            );

            Commit[] commits = exchange(url, Commit[].class);
            if (commits == null || commits.length == 0) break;

            for (Commit c : commits) {
                ZonedDateTime zdt = parseDate(c.getCommit().getAuthor().getDate());
                if (zdt != null) {
                    commitsPerYear.merge(zdt.getYear(), 1, Integer::sum);
                }
            }

            if (commits.length < ExtractRepoAndOwner.PER_PAGE) break;
            page++;
        }

        return commitsPerYear;
    }

    @Cacheable(value = "github:user-stats", key = "#username")
    public Map<String, Integer> getUserStats(String username) {
        Map<String, Integer> stats = new HashMap<>();
        try {
            log.info("Fetching GitHub stats for {}", username);

            Map<String, Object> userInfo = getUserInfo(username);
            int publicRepos = ((Number) userInfo.getOrDefault("public_repos", 0)).intValue();

            List<Map<String, Object>> allRepos = fetchAllUserRepos(username);
            int totalStars = allRepos.stream()
                    .mapToInt(repo -> ((Number) repo.getOrDefault("stargazers_count", 0)).intValue())
                    .sum();

            int[] contributionData = getContributionStats(username);

            stats.put("stars", totalStars);
            stats.put("commits", contributionData[0]);
            stats.put("prs", contributionData[1]);
            stats.put("issues", contributionData[2]);
            stats.put("contributions", Arrays.stream(contributionData).sum());
            stats.put("repositories", publicRepos);

            log.info("Stats for {}: {}", username, stats);
        } catch (Exception e) {
            log.error("Error fetching stats for {}: {}", username, e.getMessage());
            stats.putAll(getDefaultStats());
        }
        return stats;
    }

    private Map<String, Object> getUserInfo(String username) {
        return Optional.ofNullable(exchange("https://api.github.com/users/" + username, Map.class))
                .map(m -> (Map<String, Object>) m)
                .orElse(new HashMap<>());
    }

    @SuppressWarnings("unchecked")
    @Cacheable(value = "github:user-repos", key = "#username")
    public List<Map<String, Object>> fetchAllUserRepos(String username) {
        List<Map<String, Object>> allRepos = new ArrayList<>();
        int page = 1, perPage = 100;

        while (true) {
            String url = String.format(
                    "https://api.github.com/users/%s/repos?type=all&sort=updated&per_page=%d&page=%d",
                    username, perPage, page
            );

            Map<String, Object>[] repos = exchange(url, Map[].class);
            if (repos == null || repos.length == 0) break;

            allRepos.addAll(Arrays.asList(repos));

            if (repos.length < perPage || page >= 10) break;
            page++;
        }

        log.info("Fetched {} repos for {}", allRepos.size(), username);
        return allRepos;
    }


    private int[] getContributionStats(String username) {
        return new int[]{
                getSearchCount(username, "author:" + username, "commits"),
                getSearchCount(username, "is:pr author:" + username, "issues"),
                getSearchCount(username, "is:issue author:" + username, "issues")
        };
    }

    private int getSearchCount(String username, String query, String type) {
        String baseUrl = type.equals("commits")
                ? "https://api.github.com/search/commits"
                : "https://api.github.com/search/issues";

        String url = String.format("%s?q=%s&per_page=1", baseUrl, query);
        try {
            HttpHeaders headers = extractRepoAndOwner.createHeaders();
            if ("commits".equals(type)) {
                headers.add("Accept", "application/vnd.github.cloak-preview+json");
            }
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), Map.class);
            Map body = response.getBody();
            return (body != null && body.containsKey("total_count"))
                    ? Math.min(((Number) body.get("total_count")).intValue(), 1000)
                    : 0;
        } catch (HttpClientErrorException e) {
            log.warn("Failed to get {} count for {}: {}", type, username, e.getMessage());
            return 0;
        }
    }

    private Map<String, Integer> getDefaultStats() {
        return java.util.Map.of(
                "stars", 0,
                "commits", 0,
                "issues", 0,
                "prs", 0,
                "contributions", 0,
                "repositories", 0
        );
    }

    public String calculateGrade(Map<String, Integer> stats) {
        double score =
                stats.getOrDefault("stars", 0) * 2.0 +
                        stats.getOrDefault("commits", 0) +
                        stats.getOrDefault("prs", 0) * 3.0 +
                        stats.getOrDefault("issues", 0) * 1.5 +
                        stats.getOrDefault("repositories", 0) * 5.0;

        if (score >= 1000) return "A+";
        if (score >= 750) return "A";
        if (score >= 500) return "B+";
        if (score >= 250) return "B";
        if (score >= 100) return "C+";
        if (score >= 50) return "C";
        if (score >= 25) return "D";
        return "F";
    }

    @Cacheable(value = "github:top-stars", key = "#username + ':' + #limit")
    public LinkedHashMap<String, Integer> getTopStarredRepos(String username, int limit) {
        return fetchAllUserRepos(username).stream()
                .map(repo -> new AbstractMap.SimpleEntry<>(
                        (String) repo.getOrDefault("name", ""),
                        ((Number) repo.getOrDefault("stargazers_count", 0)).intValue()
                ))
                .filter(e -> !e.getKey().isBlank())
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .limit(limit > 0 ? limit : Long.MAX_VALUE)
                .collect(Collectors.toMap(
                        Map.Entry::getKey, Map.Entry::getValue,
                        (a, b) -> a, LinkedHashMap::new
                ));
    }

    @Cacheable(value = "github:commits-monthly", key = "#repoUrl + ':' + #lastNMonths")
    public LinkedHashMap<String, Integer> getCommitsPerMonthPerProject(String repoUrl, int lastNMonths) throws Exception {
        if (lastNMonths <= 0) lastNMonths = 12;
        String[] ownerRepo = extractOwnerRepo(repoUrl);
        String owner = ownerRepo[0], repo = ownerRepo[1];

        Map<String, Integer> counts = new HashMap<>();
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime firstMonthStart = now.minusMonths(lastNMonths - 1).withDayOfMonth(1).toLocalDate().atStartOfDay(now.getZone());

        int page = 1;
        boolean done = false;
        while (!done) {
            String url = String.format("https://api.github.com/repos/%s/%s/commits?per_page=%d&page=%d", owner, repo, ExtractRepoAndOwner.PER_PAGE, page);
            Commit[] commits = exchange(url, Commit[].class);
            if (commits == null || commits.length == 0) break;

            for (Commit c : commits) {
                ZonedDateTime zdt = parseDate(c.getCommit().getAuthor().getDate());
                if (zdt == null) continue;
                if (zdt.isBefore(firstMonthStart)) {
                    done = true;
                    break;
                }
                String key = String.format("%04d-%02d", zdt.getYear(), zdt.getMonthValue());
                counts.merge(key, 1, Integer::sum);
            }

            if (done || commits.length < ExtractRepoAndOwner.PER_PAGE) break;
            page++;
        }

        LinkedHashMap<String, Integer> ordered = new LinkedHashMap<>();
        for (int i = lastNMonths - 1; i >= 0; i--) {
            ZonedDateTime m = now.minusMonths(i);
            String key = String.format("%04d-%02d", m.getYear(), m.getMonthValue());
            ordered.put(key, counts.getOrDefault(key, 0));
        }
        return ordered;
    }

    @Cacheable(value = "github:code-churn-monthly", key = "#repoUrl + ':' + #lastNMonths")
    public LinkedHashMap<String, int[]> getCodeChurnPerMonth(String repoUrl, int lastNMonths) throws Exception {
        if (lastNMonths <= 0) lastNMonths = 12;
        String[] ownerRepo = extractOwnerRepo(repoUrl);
        String owner = ownerRepo[0], repo = ownerRepo[1];

        String url = String.format("https://api.github.com/repos/%s/%s/stats/code_frequency", owner, repo);
        Map<String, int[]> monthAgg = new HashMap<>();

        Object body = exchange(url, Object.class);
        if (body instanceof List) {
            for (Object w : (List<?>) body) {
                if (w instanceof List row && row.size() >= 3) {
                    long weekEpoch = ((Number) row.get(0)).longValue();
                    int additions = ((Number) row.get(1)).intValue();
                    int deletions = ((Number) row.get(2)).intValue();
                    ZonedDateTime zdt = ZonedDateTime.ofInstant(Instant.ofEpochSecond(weekEpoch), java.time.ZoneId.systemDefault());
                    String key = String.format("%04d-%02d", zdt.getYear(), zdt.getMonthValue());
                    monthAgg.computeIfAbsent(key, k -> new int[2]);
                    monthAgg.get(key)[0] += additions;
                    monthAgg.get(key)[1] += deletions;
                }
            }
        }

        ZonedDateTime now = ZonedDateTime.now();
        LinkedHashMap<String, int[]> ordered = new LinkedHashMap<>();
        for (int i = lastNMonths - 1; i >= 0; i--) {
            ZonedDateTime m = now.minusMonths(i);
            String key = String.format("%04d-%02d", m.getYear(), m.getMonthValue());
            ordered.put(key, monthAgg.getOrDefault(key, new int[]{0, 0}));
        }
        return ordered;
    }

    @Cacheable(value = "github:workflow-status", key = "#repoUrl + ':' + (#workflow == null ? '' : #workflow)")
    public String getLatestWorkflowStatus(String repoUrl, String workflow) throws Exception {
        String[] ownerRepo = extractOwnerRepo(repoUrl);
        String owner = ownerRepo[0], repo = ownerRepo[1];

        String url = String.format("https://api.github.com/repos/%s/%s/actions/runs?per_page=1", owner, repo);
        Map body = exchange(url, Map.class);

        if (body != null && body.containsKey("workflow_runs")) {
            List runs = (List) body.get("workflow_runs");
            if (runs != null && !runs.isEmpty()) {
                Map run = (Map) runs.get(0);
                Object conclusion = run.get("conclusion");
                Object status = run.get("status");
                return conclusion != null ? conclusion.toString() :
                        status != null ? status.toString() : "unknown";
            }
        }
        return "unknown";
    }

    @Cacheable(value = "github:filetype-churn", key = "#repoUrl + ':' + #limitCommits")
    public LinkedHashMap<String, int[]> getFileTypeChurn(String repoUrl, int limitCommits) throws Exception {
        if (limitCommits <= 0) limitCommits = 50;
        String[] ownerRepo = extractOwnerRepo(repoUrl);
        String owner = ownerRepo[0], repo = ownerRepo[1];

        Map<String, int[]> agg = new HashMap<>();
        int fetched = 0, page = 1;

        while (fetched < limitCommits) {
            String listUrl = String.format("https://api.github.com/repos/%s/%s/commits?per_page=%d&page=%d", owner, repo, ExtractRepoAndOwner.PER_PAGE, page);
            Commit[] commits = exchange(listUrl, Commit[].class);
            if (commits == null || commits.length == 0) break;

            for (Commit c : commits) {
                if (fetched++ >= limitCommits) break;
                String sha = c.getSha();
                if (sha == null) continue;

                String detailUrl = String.format("https://api.github.com/repos/%s/%s/commits/%s", owner, repo, sha);
                Map body = exchange(detailUrl, Map.class);
                if (body != null && body.containsKey("files")) {
                    for (Object fo : (List<?>) body.get("files")) {
                        Map f = (Map) fo;
                        String filename = (String) f.get("filename");
                        int additions = ((Number) f.getOrDefault("additions", 0)).intValue();
                        int deletions = ((Number) f.getOrDefault("deletions", 0)).intValue();
                        String ext = (filename != null && filename.contains("."))
                                ? filename.substring(filename.lastIndexOf('.') + 1).toLowerCase()
                                : "other";
                        agg.computeIfAbsent(ext, k -> new int[2]);
                        agg.get(ext)[0] += additions;
                        agg.get(ext)[1] += deletions;
                    }
                }
            }
            if (commits.length < ExtractRepoAndOwner.PER_PAGE) break;
            page++;
        }

        return agg.entrySet().stream()
                .sorted((a, b) -> Integer.compare(
                        b.getValue()[0] + b.getValue()[1],
                        a.getValue()[0] + a.getValue()[1]))
                .collect(Collectors.toMap(
                        Map.Entry::getKey, Map.Entry::getValue,
                        (a, b) -> a, LinkedHashMap::new
                ));
    }
}
