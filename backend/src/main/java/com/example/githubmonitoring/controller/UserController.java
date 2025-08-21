package com.example.githubmonitoring.controller;

import com.example.domain.Theme;
import com.example.githubmonitoring.builder.SVGBadge;
import com.example.service.BadgeService;
import com.example.service.ChartService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.Serializable;
import java.util.List;

@RestController
@AllArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class UserController {

    private final ChartService chartService;
    private final BadgeService badgeService;

    private ResponseEntity<String> svg(String svg) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "image/svg+xml")
                .body(svg);
    }

    private ResponseEntity<byte[]> png(byte[] bytes) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "image/png")
                .body(bytes);
    }

    private Theme toTheme(String theme) {
        return (theme != null && theme.equalsIgnoreCase("dark")) ? Theme.DARK : Theme.LIGHT;
    }

    @GetMapping("/user-stats-badge")
    public ResponseEntity<String> userStats(@RequestParam String username) {
        List<Serializable> stats = badgeService.getUserStats(username);
        String grade = stats.get(6).toString();
        String value = "grade " + grade;
        String svg = SVGBadge.buildBadge("user " + username, value, "#2563eb", "light");
        return svg(svg);
    }

    @GetMapping("/user-contributions-graph")
    public ResponseEntity<byte[]> userContrib(@RequestParam String username,
                                              @RequestParam(required = false) String theme) {
        byte[] bytes = chartService.getUserContributionBreakdown(username, toTheme(theme));
        return png(bytes);
    }

    @GetMapping("/user-top-stars-graph")
    public ResponseEntity<byte[]> userTopStars(@RequestParam String username,
                                               @RequestParam(defaultValue = "5") int topN,
                                               @RequestParam(required = false) String theme) {
        byte[] bytes = chartService.getTopStarredRepos(username, topN, toTheme(theme));
        return png(bytes);
    }
}
