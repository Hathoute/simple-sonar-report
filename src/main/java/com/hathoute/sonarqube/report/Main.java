package com.hathoute.sonarqube.report;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class Main {
  public static void main(final String[] args) {
    final var arguments = ArgumentParserUtil.parse(args, System::exit);
    final var config = new SonarqubeConfig(arguments::getString);
    final var client = new SonarqubeAPIClient(config.getHost(), config.getToken());
    final var formatter = new ReportFormatter(config, client);

    final var report = formatter.generateReport();
    try {
      saveReport(report.right(), config.getOutput());
    } catch (final IOException e) {
      throw new IllegalStateException("Exception raised while saving report to file", e);
    }

    System.exit(report.left() ? 0 : 1);
  }

  private static void saveReport(final String report, final String path) throws IOException {
    final var file = new File(path);
    if (!file.createNewFile()) {
      throw new IllegalStateException("Cannot overwrite existing files.");
    }

    try (final var writer = new FileWriter(file); final var printer = new PrintWriter(writer)) {
      printer.print(report);
    }
  }
}