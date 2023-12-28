package com.hathoute.sonarqube.report.model;

import java.util.List;

public record ComponentMeasures(
    String key,
    String name,
    String qualifier,
    List<Measure> measures
) {
}
