package com.hathoute.sonarqube.report;

import com.hathoute.sonarqube.report.model.ComponentWithMetrics;
import com.hathoute.sonarqube.report.model.Measure;
import com.hathoute.sonarqube.report.model.Metric;
import com.hathoute.sonarqube.report.model.ProjectStatus;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.function.Function.identity;

public class ReportFormatter {

  private static final String DEFAULT_TEMPLATE = "template1";

  private final SonarqubeConfig config;
  private final SonarqubeAPIClient client;
  private final TemplateEngine templateEngine;

  public ReportFormatter(SonarqubeConfig config) {
    this.config = config;
    this.client = new SonarqubeAPIClient(config.getHost(), config.getToken());

    var templateResolver = new ClassLoaderTemplateResolver();
    templateResolver.setPrefix("/templates/");
    templateResolver.setSuffix(".md");
    templateResolver.setTemplateMode(TemplateMode.TEXT);
    templateResolver.setCharacterEncoding("UTF8");
    templateResolver.setCheckExistence(true);

    templateEngine = new TemplateEngine();
    templateEngine.addTemplateResolver(templateResolver);
  }

  public String generateReport() {
    var projectKey = config.getProjectKey();
    var measuresWithMetrics = client.getComponentMeasures(projectKey);
    var status = client.getProjectStatus(projectKey);
    var templateMetrics = buildTemplateMetrics(measuresWithMetrics);

    var context = new Context();
    addStatusVariable(status, context);
    addProjectVariables(projectKey, config.getHost(), context);
    context.setVariable("metrics", templateMetrics);

    return templateEngine.process(DEFAULT_TEMPLATE, context);
  }

  private static void addStatusVariable(ProjectStatus projectStatus, Context context) {
    var passed = projectStatus.status().equals("OK");
    context.setVariable("gate_status_color", passed ? "green" : "red");
    context.setVariable("gate_status", projectStatus.status());
  }

  private static void addProjectVariables(String projectKey, String hostUrl, Context context) {
    context.setVariable("project_name", projectKey);
    context.setVariable("project_dashboard_url", hostUrl + (hostUrl.endsWith("/") ? "" : "/")
        + "dashboard?id=" + projectKey);
  }

  private static List<TemplateMetric> buildTemplateMetrics(ComponentWithMetrics measuresWithMetrics) {
    var componentMeasures = measuresWithMetrics.component();
    var metrics = measuresWithMetrics.metrics().stream()
        .collect(Collectors.toMap(Metric::key, identity()));

    return componentMeasures.measures()
        .stream()
        .map(m -> TemplateMetric.of(metrics.get(m.metric()), m))
        .sorted(Comparator.comparingInt(m -> SonarqubeAPIClient.INCLUDED_METRICS.indexOf(m.key)))
        .toList();
  }

  record TemplateMetric(String key, String name, String description, String value) {
    static TemplateMetric of(Metric metric, Measure measure) {
      return new TemplateMetric(metric.key(), metric.name(), metric.description(), measure.value());
    }
  }
}
