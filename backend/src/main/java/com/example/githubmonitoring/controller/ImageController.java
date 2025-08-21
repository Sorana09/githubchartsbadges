package com.example.githubmonitoring.controller;

import com.example.domain.Theme;
import com.example.service.ChartService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@AllArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")

public class ImageController {

    private final ChartService chartService;

    private ResponseEntity<byte[]> png(byte[] bytes) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "image/png")
                .body(bytes);
    }

    private Theme toTheme(String theme) {
        return (theme != null && theme.equalsIgnoreCase("dark")) ? Theme.DARK : Theme.LIGHT;
    }

    @GetMapping("/commits-yearly-graph-project")
    public ResponseEntity<byte[]> commitsYearly(@RequestParam String repoUrl,
                                                @RequestParam(required = false) String theme) {
        byte[] bytes = chartService.getCommitPerYearChart(repoUrl, toTheme(theme));
        return png(bytes);
    }

    @GetMapping("/commits-monthly-line-graph-project")
    public ResponseEntity<byte[]> commitsMonthlyLine(@RequestParam String repoUrl,
                                                     @RequestParam(defaultValue = "12") int months,
                                                     @RequestParam(required = false) String theme) {
        byte[] bytes = chartService.getCommitsPerMonthLineChart(repoUrl, months, toTheme(theme));
        return png(bytes);
    }

    @GetMapping("/code-churn-monthly")
    public ResponseEntity<byte[]> codeChurnMonthly(@RequestParam String repoUrl,
                                                   @RequestParam(defaultValue = "12") int months,
                                                   @RequestParam(required = false) String theme) {
        byte[] bytes = chartService.getCodeChurnChart(repoUrl, months, toTheme(theme));
        return png(bytes);
    }

    @GetMapping("/rollup/code-churn-monthly")
    public ResponseEntity<byte[]> codeChurnMonthlyRollup(@RequestParam String repos,
                                                         @RequestParam(defaultValue = "12") int months,
                                                         @RequestParam(required = false) String theme) {
        List<String> list = Arrays.stream(repos.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
        byte[] bytes = chartService.getCodeChurnChartForRepos(list, months, toTheme(theme));
        return png(bytes);
    }

    @GetMapping("/filetype-churn")
    public ResponseEntity<byte[]> filetypeChurn(@RequestParam String repoUrl,
                                                @RequestParam(defaultValue = "50") int limit,
                                                @RequestParam(defaultValue = "8") int topN,
                                                @RequestParam(required = false) String theme) {
        byte[] bytes = chartService.getFileTypeChurnChart(repoUrl, limit, topN, toTheme(theme));
        return png(bytes);
    }
}
