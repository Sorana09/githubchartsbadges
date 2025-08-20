package com.example.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Commit {
    private String sha;
    private InnerCommit commit;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class InnerCommit {
        private Author author;

        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Author {
            private String date;
        }
    }
}
