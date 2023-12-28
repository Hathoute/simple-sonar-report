package com.hathoute.sonarqube.report;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class Main {
  public static void main(String[] args) {
    var arguments = ArgumentParserUtil.parse(args);
    var config = new SonarqubeConfig(arguments::getString);
    var formatter = new ReportFormatter(config);

    var report = formatter.generateReport();
    try {
      saveReport(report, config.getOutput());
    } catch (IOException e) {
      throw new IllegalStateException("Exception raised while saving report to file", e);
    }
  }

  private static void saveReport(String report, String path) throws IOException {
    var file = new File(path);
    if (!file.createNewFile()) {
      throw new IllegalStateException("Cannot overwrite existing files.");
    }

    try (var writer = new FileWriter(file); var printer = new PrintWriter(writer)) {
      printer.print(report);
    }
  }
}