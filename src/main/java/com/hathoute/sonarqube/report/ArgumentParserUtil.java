package com.hathoute.sonarqube.report;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

public class ArgumentParserUtil {

  private static final String APP_NAME = "java -jar simple-sonar-report.jar";
  private static final String APP_DESCRIPTION = "Generate a Markdown report for a project analysis";

  public static final String PROJECT_KEY_ARG_NAME = "projectkey";
  public static final String URL_ARG_NAME = "url";
  public static final String TOKEN_ARG_NAME = "token";
  public static final String OUTPUT_ARG_NAME = "output";

  private static ArgumentParser newParser() {
    var parser = ArgumentParsers.newFor(APP_NAME).build()
        .defaultHelp(true)
        .description(APP_DESCRIPTION);

    parser.addArgument("-p", "--" + PROJECT_KEY_ARG_NAME)
        .help("The sonarqube project key")
        .required(true);

    parser.addArgument("-u", "--" + URL_ARG_NAME)
        .help("The sonarqube instance host url")
        .required(true);

    parser.addArgument("-t", "--" + TOKEN_ARG_NAME)
        .help("The sonarqube authentication token")
        .required(true);

    parser.addArgument("-o", "--" + OUTPUT_ARG_NAME)
        .help("Formatted markdown output")
        .setDefault("./report-out.md")
        .required(false);

    return parser;
  }

  public static Namespace parse(String[] args) {
    var parser = newParser();
    try {
      return parser.parseArgs(args);
    } catch (ArgumentParserException e) {
      parser.handleError(e);
      System.exit(-1);
      return null;
    }
  }
}
