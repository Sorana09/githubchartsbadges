package com.example.service;

import com.example.domain.ChartInformation;
import com.example.domain.ChartType;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Map;

@Service
@AllArgsConstructor
@Slf4j
public class ChartService {
    private final GithubService githubService;

    public byte[] getCommitPerYearChart(String repoUrl) {
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
