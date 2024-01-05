package com.hathoute.sonarqube.report;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hathoute.sonarqube.report.model.ComponentWithMetrics;
import com.hathoute.sonarqube.report.model.ProjectStatus;
import com.hathoute.sonarqube.report.model.ProjectStatus.WrappedProjectStatus;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

public class SonarqubeAPIClient {
  private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(15);
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().configure(
      DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

  private final String hostUrl;
  private final String userToken;

  public SonarqubeAPIClient(final String hostUrl, final String userToken) {
    this.hostUrl = hostUrl;
    this.userToken = userToken;
  }

  public ComponentWithMetrics getComponentMeasures(final String projectKey, final String pullRequestId,
      final List<String> metrics) {
    var url = "api/measures/component?additionalFields=period%2Cmetrics&component=" + projectKey
        + "&metricKeys=" + String.join("%2C", metrics);
    if (!pullRequestId.isBlank()) {
      url += "&pullRequest=" + pullRequestId;
    }
    return get(url, ComponentWithMetrics.class);
  }

  public ProjectStatus getProjectStatus(final String projectKey) {
    final var url = "api/qualitygates/project_status?projectKey=" + projectKey;
    return get(url, WrappedProjectStatus.class).projectStatus();
  }

  private <T> T get(final String path, final Class<T> responseClass) {
    final var request = HttpRequest.newBuilder()
                                   .uri(uri(path))
                                   .headers("Authorization", "Bearer " + this.userToken)
                                   .timeout(REQUEST_TIMEOUT)
                                   .GET()
                                   .build();

    try {
      final var response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
      if (response.statusCode() != 200) {
        throw new IllegalStateException(
            "Received status code %d: %s".formatted(response.statusCode(), response.body()));
      }
      return OBJECT_MAPPER.readValue(response.body(), responseClass);
    } catch (final IOException e) {
      throw new IllegalStateException("Exception when sending HTTP Request", e);
    } catch (final InterruptedException e) {
      Thread.currentThread().interrupt();
      return null;
    }
  }

  private URI uri(final String path) {
    var separator = "";
    if (!hostUrl.endsWith("/")) {
      separator = "/";
    }

    return URI.create(hostUrl + separator + path);
  }
}
