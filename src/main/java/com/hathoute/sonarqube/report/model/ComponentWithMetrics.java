package com.hathoute.sonarqube.report.model;

import java.util.List;

public record ComponentWithMetrics(ComponentMeasures component, List<Metric> metrics) {
}
