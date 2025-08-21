package com.example.githubmonitoring.controller;

import com.example.githubmonitoring.builder.SVGBadge;
import com.example.service.BadgeService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/badge")
@AllArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class BadgeController {

    private final BadgeService badgeService;

    private ResponseEntity<String> svg(String svg) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "image/svg+xml")
                .body(svg);
    }

    private String normTheme(String theme) {
        return (theme != null && theme.equalsIgnoreCase("dark")) ? "dark" : "light";
    }

    @GetMapping("/stars")
    public ResponseEntity<String> stars(@RequestParam String repoUrl,
                                        @RequestParam(required = false) String theme) throws Exception {
        int v = badgeService.getStars(repoUrl);
        String svg = SVGBadge.buildBadge("stars", String.valueOf(v), "#f59e0b", normTheme(theme));
        return svg(svg);
    }

    @GetMapping("/language")
    public ResponseEntity<String> language(@RequestParam String repoUrl,
                                           @RequestParam(required = false) String theme) throws Exception {
        String v = badgeService.getLanguage(repoUrl);
        String svg = SVGBadge.buildBadge("language", v == null ? "" : v, "#06b6d4", normTheme(theme));
        return svg(svg);
    }

    @GetMapping("/issues")
    public ResponseEntity<String> issues(@RequestParam String repoUrl,
                                         @RequestParam(required = false) String theme) throws Exception {
        int v = badgeService.getIssues(repoUrl);
        String svg = SVGBadge.buildBadge("issues", String.valueOf(v), "#ef4444", normTheme(theme));
        return svg(svg);
    }

    @GetMapping("/forks")
    public ResponseEntity<String> forks(@RequestParam String repoUrl,
                                        @RequestParam(required = false) String theme) throws Exception {
        int v = badgeService.getForks(repoUrl);
        String svg = SVGBadge.buildBadge("forks", String.valueOf(v), "#22c55e", normTheme(theme));
        return svg(svg);
    }

    @GetMapping("/watchers")
    public ResponseEntity<String> watchers(@RequestParam String repoUrl,
                                           @RequestParam(required = false) String theme) throws Exception {
        int v = badgeService.getWatchers(repoUrl);
        String svg = SVGBadge.buildBadge("watchers", String.valueOf(v), "#6366f1", normTheme(theme));
        return svg(svg);
    }

    @GetMapping("/last-commit")
    public ResponseEntity<String> lastCommit(@RequestParam String repoUrl,
                                             @RequestParam(required = false) String theme) throws Exception {
        String v = badgeService.getLastCommitDate(repoUrl);
        String svg = SVGBadge.buildBadge("last commit", v, "#14b8a6", normTheme(theme));
        return svg(svg);
    }

    @GetMapping("/created")
    public ResponseEntity<String> created(@RequestParam String repoUrl,
                                          @RequestParam(required = false) String theme) throws Exception {
        String v = badgeService.getCreatedAt(repoUrl);
        String svg = SVGBadge.buildBadge("created", v, "#64748b", normTheme(theme));
        return svg(svg);
    }

    @GetMapping("/language-icon")
    public ResponseEntity<String> languageIcon(@RequestParam String repoUrl) throws Exception {
        String lang = badgeService.getLanguage(repoUrl);
        String svg = SVGBadge.buildBadge("lang", lang == null ? "" : lang, "#0ea5e9", "light");
        return svg(svg);
    }

    @GetMapping("/ci-status")
    public ResponseEntity<String> ciStatus(@RequestParam String repoUrl,
                                           @RequestParam(required = false) String workflow,
                                           @RequestParam(required = false) String theme) throws Exception {
        var parts = badgeService.getCiStatusBadge(repoUrl, workflow, normTheme(theme));
        String label = parts.get(0).toString();
        String status = parts.get(1).toString();
        String color = parts.get(2).toString();
        String th = parts.get(3).toString();
        String svg = SVGBadge.buildBadge(label, status, color, th);
        return svg(svg);
    }

    @GetMapping("/coverage")
    public ResponseEntity<String> coverage(@RequestParam int value,
                                           @RequestParam(required = false) String theme) {
        int v = Math.max(0, Math.min(100, value));
        String color = v >= 90 ? "#22c55e" : v >= 75 ? "#84cc16" : v >= 60 ? "#eab308" : v >= 40 ? "#f97316" : "#ef4444";
        String svg = SVGBadge.buildBadge("coverage", v + "%", color, normTheme(theme));
        return svg(svg);
    }
}
