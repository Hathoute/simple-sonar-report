package com.hathoute.sonarqube.report.model;

public record Metric(
    String key,
    String name,
    String description
) {
}
