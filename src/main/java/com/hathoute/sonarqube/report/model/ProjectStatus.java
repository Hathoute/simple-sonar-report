package com.hathoute.sonarqube.report.model;

public record ProjectStatus(String status) {
  public record WrappedProjectStatus(ProjectStatus projectStatus) {
  }
}
