package com.example.githubmonitoring;

import lombok.Builder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.example")
public class GithubmonitoringApplication {
	public static void main(String[] args) {
        SpringApplication.run(GithubmonitoringApplication.class, args);
	}

}
