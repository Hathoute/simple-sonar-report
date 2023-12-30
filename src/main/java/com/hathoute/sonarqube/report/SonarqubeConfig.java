package com.hathoute.sonarqube.report;

import java.util.HashMap;
import java.util.Map;
import java.util.function.UnaryOperator;

import static com.hathoute.sonarqube.report.ArgumentParserUtil.*;
import static java.util.Objects.isNull;

public class SonarqubeConfig {

  private final UnaryOperator<String> valueProvider;
  private final Map<String, String> cachedValues;

  public SonarqubeConfig(UnaryOperator<String> valueProvider) {
    this.valueProvider = valueProvider;
    cachedValues = new HashMap<>();
  }

  public String getProjectKey() {
    return getValue(PROJECT_KEY_ARG_NAME);
  }

  public String getHost() {
    return getValue(URL_ARG_NAME);
  }

  public String getToken() {
    return getValue(TOKEN_ARG_NAME);
  }

  public String getOutput() {
    return getValue(OUTPUT_ARG_NAME);
  }

  public String getTemplate() {
    return getValue(TEMPLATE_ARG_NAME);
  }

  public String getPullRequest() {
    return getValue(PULL_REQUEST_ARG_NAME);
  }

  private String getValue(String key) {
    var value = cachedValues.computeIfAbsent(key, valueProvider);
    if (isNull(value)) {
      // Throw an exception instead of logging + stopping execution since this is
      // an unexpected behaviour: all required/optional arguments must be provided
      // either by the user or the parser at this point.
      throw new IllegalStateException("Could not find value for key '%s' in parsed arguments".formatted(key));
    }
    return value;
  }
}
