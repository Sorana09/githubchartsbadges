package com.example.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class Stats {
    private int stars;
    private int commits;
    private int prs;
    private int issues;
    private int contributions;
    private int repositories;
}
