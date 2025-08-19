package com.example.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@AllArgsConstructor
@Data
public class RepoInformation {
    private final String owner;
    private final String repo;
}
