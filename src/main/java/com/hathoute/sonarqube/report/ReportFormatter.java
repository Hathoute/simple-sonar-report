package com.hathoute.sonarqube.report;

import com.hathoute.sonarqube.report.model.ComponentWithMetrics;
import com.hathoute.sonarqube.report.model.Measure;
import com.hathoute.sonarqube.report.model.Metric;
import com.hathoute.sonarqube.report.model.ProjectStatus;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.hathoute.sonarqube.report.ReportFormatter.MetricDefinition.of;
import static java.util.function.Function.identity;

public class ReportFormatter {

  private static final List<MetricDefinition> METRICS = List.of(
      of("lines"), of("bugs"), of("coverage"), of("code_smells"), of("duplicated_lines_density"), of("new_maintainability_rating", false),
      of("new_reliability_rating", false), of("vulnerabilities")
  );

  private final SonarqubeConfig config;
  private final SonarqubeAPIClient client;
  private final TemplateEngine templateEngine;

  public ReportFormatter(SonarqubeConfig config, SonarqubeAPIClient client) {
    this.config = config;
    this.client = client;

    var templateResolver = new ClassLoaderTemplateResolver();
    templateResolver.setPrefix("/templates/");
    templateResolver.setSuffix(".md");
    templateResolver.setTemplateMode(TemplateMode.TEXT);
    templateResolver.setCharacterEncoding("UTF8");
    templateResolver.setCheckExistence(true);

    templateEngine = new TemplateEngine();
    templateEngine.addTemplateResolver(templateResolver);
  }

  public Pair<Boolean, String> generateReport() {
    var projectKey = config.getProjectKey();
    var isPullRequest = !config.getPullRequest().isBlank();
    var metricsToInclude = fromMetrics(METRICS, isPullRequest);
    var measuresWithMetrics = client.getComponentMeasures(projectKey, config.getPullRequest(), metricsToInclude);
    var status = client.getProjectStatus(projectKey);
    var templateMetrics = buildTemplateMetrics(measuresWithMetrics, METRICS);

    var context = new Context();
    addStatusVariable(status, context);
    addProjectVariables(config, context);
    context.setVariable("metrics", templateMetrics);

    var report = templateEngine.process(config.getTemplate(), context);
    var passed = status.status().equals("OK");
    return new Pair<>(passed, report);
  }

  private static void addStatusVariable(ProjectStatus projectStatus, Context context) {
    var passed = projectStatus.status().equals("OK");
    context.setVariable("gate_status_bool", passed);
    context.setVariable("gate_status", projectStatus.status());
  }

  private static void addProjectVariables(SonarqubeConfig config, Context context) {
    var projectKey = config.getProjectKey();
    var hostUrl = config.getHost();
    var pullRequest = config.getPullRequest();
    var projectUrl = hostUrl + (hostUrl.endsWith("/") ? "" : "/")
        + "dashboard?id=" + projectKey + (pullRequest.isBlank() ? "" : "&pullRequest=" + pullRequest);
    context.setVariable("project_name", projectKey);
    context.setVariable("project_dashboard_url", projectUrl);
  }

  private static List<TemplateMetric> buildTemplateMetrics(ComponentWithMetrics measuresWithMetrics, List<MetricDefinition> metricsToInclude) {
    var componentMeasures = measuresWithMetrics.component();
    var metricMap = measuresWithMetrics.metrics()
        .stream()
        .collect(Collectors.toMap(Metric::key, identity()));
    var measureMap = componentMeasures.measures()
        .stream()
        .collect(Collectors.toMap(Measure::metric, identity()));

    return metricsToInclude.stream()
        .filter(md -> measureMap.containsKey(md.key))
        .map(md -> TemplateMetric.of(metricMap.get(md.key), measureMap.get(md.key), measureMap.get("new_" + md.key)))
        .toList();
  }

  private static List<String> fromMetrics(List<MetricDefinition> metrics, boolean isPullRequest) {
    if (isPullRequest) {
      return metrics.stream().flatMap(MetricDefinition::keys).toList();
    }

    return metrics.stream().map(MetricDefinition::key).toList();
  }

  record MetricDefinition(String key, boolean canBeNew) {
    static MetricDefinition of(String key, boolean canBeNew) {
      return new MetricDefinition(key, canBeNew);
    }

    static MetricDefinition of(String key) {
      return of(key, true);
    }

    Stream<String> keys() {
      return canBeNew ? Stream.of(key, "new_" + key) : Stream.of(key);
    }
  }

  record TemplateMetric(String key, String name, String description, String value, Optional<String> newValue) {
    static TemplateMetric of(Metric metric, Measure measure, Measure newValue) {
      return new TemplateMetric(metric.key(), metric.name(), metric.description(), measure.value(), Optional.ofNullable(newValue)
          .map(Measure::value));
    }
  }
}
