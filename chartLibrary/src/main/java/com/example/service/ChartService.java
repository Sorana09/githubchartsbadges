package com.example.service;

import com.example.domain.ChartInformation;
import com.example.domain.ChartType;
import com.example.domain.Theme;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@AllArgsConstructor
@Slf4j
public class ChartService {
    private final GithubService githubService;

    // Helper: default to LIGHT if null
    private Theme normalize(Theme theme) { return theme == null ? Theme.LIGHT : theme; }

    public byte[] getCommitPerYearChart(String repoUrl) {
        return getCommitPerYearChart(repoUrl, Theme.LIGHT);
    }

    public byte[] getCommitPerYearChart(String repoUrl, Theme theme) {
        try {
            Map<Integer, Integer> commitsPerYear = githubService.getCommitsPerYearPerProject(repoUrl);

            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            for (Map.Entry<Integer, Integer> entry : commitsPerYear.entrySet()) {
                dataset.addValue(entry.getValue(), "Commits", entry.getKey().toString());
            }

            JFreeChart barChart = new GenerateChartService().buildChart(
                    ChartInformation.builder()
                            .chartType(ChartType.BAR)
                            .title(String.format("Commits per Year for %s", ExtractRepoAndOwner.extractOwnerAndRepo(repoUrl).getRepo()))
                            .categoryAxisLabel("Year")
                            .valueAxisLabel("Commits")
                            .categoryDataset(dataset)
                            .theme(normalize(theme))
                            .build()
            );

            BufferedImage chartImage = barChart.createBufferedImage(800, 400);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(chartImage, "png", baos);

            return baos.toByteArray();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public byte[] getUserContributionBreakdown(String username) {
        return getUserContributionBreakdown(username, Theme.LIGHT);
    }

    public byte[] getUserContributionBreakdown(String username, Theme theme) {
        try {
            Map<String, Integer> stats = githubService.getUserStats(username);
            int commits = stats.getOrDefault("commits", 0);
            int prs = stats.getOrDefault("prs", 0);
            int issues = stats.getOrDefault("issues", 0);

            org.jfree.data.general.DefaultPieDataset<String> pieDataset = new org.jfree.data.general.DefaultPieDataset<>();
            pieDataset.setValue("Commits", commits);
            pieDataset.setValue("PRs", prs);
            pieDataset.setValue("Issues", issues);

            JFreeChart pieChart = new GenerateChartService().buildChart(
                    ChartInformation.builder()
                            .chartType(ChartType.PIE)
                            .title(String.format("%s: Contributions Breakdown", username))
                            .pieDataset(pieDataset)
                            .theme(normalize(theme))
                            .build()
            );

            BufferedImage chartImage = pieChart.createBufferedImage(600, 400);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(chartImage, "png", baos);
            return baos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public byte[] getTopStarredRepos(String username, int topN) {
        return getTopStarredRepos(username, topN, Theme.LIGHT);
    }

    public byte[] getTopStarredRepos(String username, int topN, Theme theme) {
        try {
            Map<String, Integer> topStars = githubService.getTopStarredRepos(username, topN);
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            for (Map.Entry<String, Integer> entry : topStars.entrySet()) {
                dataset.addValue(entry.getValue(), "Stars", entry.getKey());
            }

            JFreeChart barChart = new GenerateChartService().buildChart(
                    ChartInformation.builder()
                            .chartType(ChartType.BAR)
                            .title(String.format("Top %d Starred Repositories for %s", topN, username))
                            .categoryAxisLabel("Repository")
                            .valueAxisLabel("Stars")
                            .categoryDataset(dataset)
                            .theme(normalize(theme))
                            .build()
            );

            BufferedImage chartImage = barChart.createBufferedImage(800, 400);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(chartImage, "png", baos);
            return baos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public byte[] getCommitsPerMonthLineChart(String repoUrl, int months) {
        return getCommitsPerMonthLineChart(repoUrl, months, Theme.LIGHT);
    }

    public byte[] getCommitsPerMonthLineChart(String repoUrl, int months, Theme theme) {
        try {
            Map<String, Integer> perMonth = githubService.getCommitsPerMonthPerProject(repoUrl, months);
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            for (Map.Entry<String, Integer> e : perMonth.entrySet()) {
                dataset.addValue(e.getValue(), "Commits", e.getKey());
            }

            JFreeChart lineChart = new GenerateChartService().buildChart(
                    ChartInformation.builder()
                            .chartType(ChartType.LINE)
                            .title(String.format("Commits per Month (last %d) for %s", months, ExtractRepoAndOwner.extractOwnerAndRepo(repoUrl).getRepo()))
                            .categoryAxisLabel("Month")
                            .valueAxisLabel("Commits")
                            .categoryDataset(dataset)
                            .theme(normalize(theme))
                            .build()
            );

            BufferedImage chartImage = lineChart.createBufferedImage(800, 400);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(chartImage, "png", baos);
            return baos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Code churn (additions vs deletions per month)
    public byte[] getCodeChurnChart(String repoUrl, int months, Theme theme) {
        try {
            LinkedHashMap<String, int[]> perMonth = githubService.getCodeChurnPerMonth(repoUrl, months);
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            for (Map.Entry<String, int[]> e : perMonth.entrySet()) {
                int[] v = e.getValue();
                dataset.addValue(v[0], "Additions", e.getKey());
                dataset.addValue(Math.abs(v[1]), "Deletions", e.getKey());
            }

            JFreeChart barChart = new GenerateChartService().buildChart(
                    ChartInformation.builder()
                            .chartType(ChartType.BAR)
                            .title(String.format("Code Churn (last %d months)", months))
                            .categoryAxisLabel("Month")
                            .valueAxisLabel("Lines Changed")
                            .categoryDataset(dataset)
                            .theme(normalize(theme))
                            .build()
            );
            BufferedImage chartImage = barChart.createBufferedImage(800, 400);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(chartImage, "png", baos);
            return baos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Multi-repo rollup for churn
    public byte[] getCodeChurnChartForRepos(java.util.List<String> repoUrls, int months, Theme theme) {
        try {
            // Aggregate per-month additions/deletions across repos
            LinkedHashMap<String, int[]> agg = new LinkedHashMap<>();
            for (String repoUrl : repoUrls) {
                LinkedHashMap<String, int[]> pm = githubService.getCodeChurnPerMonth(repoUrl, months);
                for (Map.Entry<String, int[]> e : pm.entrySet()) {
                    int[] cur = agg.getOrDefault(e.getKey(), new int[]{0,0});
                    cur[0] += e.getValue()[0];
                    cur[1] += e.getValue()[1];
                    agg.put(e.getKey(), cur);
                }
            }
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            for (Map.Entry<String, int[]> e : agg.entrySet()) {
                dataset.addValue(e.getValue()[0], "Additions", e.getKey());
                dataset.addValue(Math.abs(e.getValue()[1]), "Deletions", e.getKey());
            }
            JFreeChart barChart = new GenerateChartService().buildChart(
                    ChartInformation.builder()
                            .chartType(ChartType.BAR)
                            .title(String.format("Code Churn Rollup (last %d months)", months))
                            .categoryAxisLabel("Month")
                            .valueAxisLabel("Lines Changed")
                            .categoryDataset(dataset)
                            .theme(normalize(theme))
                            .build()
            );
            BufferedImage chartImage = barChart.createBufferedImage(800, 400);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(chartImage, "png", baos);
            return baos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // File-type churn (by extension) for last N commits; bar chart by total changes
    public byte[] getFileTypeChurnChart(String repoUrl, int limitCommits, int topN, Theme theme) {
        try {
            LinkedHashMap<String, int[]> churn = githubService.getFileTypeChurn(repoUrl, limitCommits);
            // reduce to topN by total changes
            java.util.List<Map.Entry<String, int[]>> list = new java.util.ArrayList<>(churn.entrySet());
            list = list.subList(0, Math.min(topN > 0 ? topN : 8, list.size()));
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            for (Map.Entry<String, int[]> e : list) {
                String ext = e.getKey();
                int adds = e.getValue()[0];
                int dels = Math.abs(e.getValue()[1]);
                dataset.addValue(adds, "Additions", ext);
                dataset.addValue(dels, "Deletions", ext);
            }
            JFreeChart barChart = new GenerateChartService().buildChart(
                    ChartInformation.builder()
                            .chartType(ChartType.BAR)
                            .title(String.format("File-type Churn (last %d commits)", limitCommits))
                            .categoryAxisLabel("Extension")
                            .valueAxisLabel("Lines Changed")
                            .categoryDataset(dataset)
                            .theme(normalize(theme))
                            .build()
            );
            BufferedImage chartImage = barChart.createBufferedImage(800, 400);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(chartImage, "png", baos);
            return baos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
