package com.example.service;

import com.example.domain.ChartInformation;
import com.example.domain.ChartType;
import com.example.domain.Theme;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
@Slf4j
public class ChartService {
    private final GithubService githubService;
    private final GenerateChartService chartService = new GenerateChartService();

    private Theme normalize(Theme theme) {
        return theme == null ? Theme.LIGHT : theme;
    }

    public byte[] getCommitPerYearChart(String repoUrl, Theme theme) {
        try {
            Map<Integer, Integer> commitsPerYear = githubService.getCommitsPerYearPerProject(repoUrl);

            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            commitsPerYear.forEach((year, commits) ->
                    dataset.addValue(commits, "Commits", year.toString())
            );

            return chartService.generateChart(
                    ChartInformation.builder()
                            .chartType(ChartType.BAR)
                            .title(String.format("Commits per Year for %s",
                                    ExtractRepoAndOwner.extractOwnerAndRepo(repoUrl).getRepo()))
                            .categoryAxisLabel("Year")
                            .valueAxisLabel("Commits")
                            .categoryDataset(dataset)
                            .theme(normalize(theme))
                            .build()
            );
        } catch (Exception e) {
            log.error("Error generating commit per year chart", e);
            return null;
        }
    }

    public byte[] getUserContributionBreakdown(String username, Theme theme) {
        try {
            Map<String, Integer> stats = githubService.getUserStats(username);

            DefaultPieDataset<String> pieDataset = new DefaultPieDataset<>();
            pieDataset.setValue("Commits", stats.getOrDefault("commits", 0));
            pieDataset.setValue("PRs", stats.getOrDefault("prs", 0));
            pieDataset.setValue("Issues", stats.getOrDefault("issues", 0));

            return chartService.generateChart(
                    ChartInformation.builder()
                            .chartType(ChartType.PIE)
                            .title(String.format("%s: Contributions Breakdown", username))
                            .pieDataset(pieDataset)
                            .theme(normalize(theme))
                            .build()
            );
        } catch (Exception e) {
            log.error("Error generating contribution breakdown chart", e);
            return null;
        }
    }

    public byte[] getTopStarredRepos(String username, int topN, Theme theme) {
        try {
            Map<String, Integer> topStars = githubService.getTopStarredRepos(username, topN);

            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            topStars.forEach((repo, stars) ->
                    dataset.addValue(stars, "Stars", repo)
            );

            return chartService.generateChart(
                    ChartInformation.builder()
                            .chartType(ChartType.BAR)
                            .title(String.format("Top %d Starred Repositories for %s", topN, username))
                            .categoryAxisLabel("Repository")
                            .valueAxisLabel("Stars")
                            .categoryDataset(dataset)
                            .theme(normalize(theme))
                            .build()
            );
        } catch (Exception e) {
            log.error("Error generating top starred repos chart", e);
            return null;
        }
    }


    public byte[] getCommitsPerMonthLineChart(String repoUrl, int months, Theme theme) {
        try {
            Map<String, Integer> perMonth = githubService.getCommitsPerMonthPerProject(repoUrl, months);

            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            perMonth.forEach((month, commits) ->
                    dataset.addValue(commits, "Commits", month)
            );

            return chartService.generateChart(
                    ChartInformation.builder()
                            .chartType(ChartType.LINE)
                            .title(String.format("Commits per Month (last %d) for %s",
                                    months, ExtractRepoAndOwner.extractOwnerAndRepo(repoUrl).getRepo()))
                            .categoryAxisLabel("Month")
                            .valueAxisLabel("Commits")
                            .categoryDataset(dataset)
                            .theme(normalize(theme))
                            .build()
            );
        } catch (Exception e) {
            log.error("Error generating commits per month chart", e);
            return null;
        }
    }

    public byte[] getCodeChurnChart(String repoUrl, int months, Theme theme) {
        try {
            LinkedHashMap<String, int[]> perMonth = githubService.getCodeChurnPerMonth(repoUrl, months);

            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            perMonth.forEach((month, values) -> {
                dataset.addValue(values[0], "Additions", month);
                dataset.addValue(Math.abs(values[1]), "Deletions", month);
            });

            return chartService.generateChart(
                    ChartInformation.builder()
                            .chartType(ChartType.BAR)
                            .title(String.format("Code Churn (last %d months)", months))
                            .categoryAxisLabel("Month")
                            .valueAxisLabel("Lines Changed")
                            .categoryDataset(dataset)
                            .theme(normalize(theme))
                            .build()
            );
        } catch (Exception e) {
            log.error("Error generating code churn chart", e);
            return null;
        }
    }

    public byte[] getCodeChurnChartForRepos(List<String> repoUrls, int months, Theme theme) {
        try {
            LinkedHashMap<String, int[]> agg = new LinkedHashMap<>();

            for (String repoUrl : repoUrls) {
                LinkedHashMap<String, int[]> pm = githubService.getCodeChurnPerMonth(repoUrl, months);
                pm.forEach((month, values) -> {
                    int[] cur = agg.getOrDefault(month, new int[]{0, 0});
                    cur[0] += values[0];
                    cur[1] += values[1];
                    agg.put(month, cur);
                });
            }

            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            agg.forEach((month, values) -> {
                dataset.addValue(values[0], "Additions", month);
                dataset.addValue(Math.abs(values[1]), "Deletions", month);
            });

            return chartService.generateChart(
                    ChartInformation.builder()
                            .chartType(ChartType.BAR)
                            .title(String.format("Code Churn Rollup (last %d months)", months))
                            .categoryAxisLabel("Month")
                            .valueAxisLabel("Lines Changed")
                            .categoryDataset(dataset)
                            .theme(normalize(theme))
                            .build()
            );
        } catch (Exception e) {
            log.error("Error generating code churn rollup chart", e);
            return null;
        }
    }

    public byte[] getFileTypeChurnChart(String repoUrl, int limitCommits, int topN, Theme theme) {
        try {
            LinkedHashMap<String, int[]> churn = githubService.getFileTypeChurn(repoUrl, limitCommits);

            List<Map.Entry<String, int[]>> list = new java.util.ArrayList<>(churn.entrySet());
            list = list.subList(0, Math.min(topN > 0 ? topN : 8, list.size()));

            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            list.forEach(entry -> {
                String ext = entry.getKey();
                int adds = entry.getValue()[0];
                int dels = Math.abs(entry.getValue()[1]);
                dataset.addValue(adds, "Additions", ext);
                dataset.addValue(dels, "Deletions", ext);
            });

            return chartService.generateChart(
                    ChartInformation.builder()
                            .chartType(ChartType.BAR)
                            .title(String.format("File-type Churn (last %d commits)", limitCommits))
                            .categoryAxisLabel("Extension")
                            .valueAxisLabel("Lines Changed")
                            .categoryDataset(dataset)
                            .theme(normalize(theme))
                            .build()
            );
        } catch (Exception e) {
            log.error("Error generating file-type churn chart", e);
            return null;
        }
    }
}
