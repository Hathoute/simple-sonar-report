package com.hathoute.sonarqube.report;

import java.util.function.IntConsumer;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

public class ArgumentParserUtil {

  private static final String APP_NAME = "java -jar simple-sonar-report.jar";
  private static final String APP_DESCRIPTION = "Generate a Markdown report for a project analysis";

  public static final String PROJECT_KEY_ARG_NAME = "projectkey";
  public static final String PULL_REQUEST_ARG_NAME = "pullrequest";
  public static final String URL_ARG_NAME = "url";
  public static final String TOKEN_ARG_NAME = "token";
  public static final String OUTPUT_ARG_NAME = "output";
  public static final String TEMPLATE_ARG_NAME = "template";

  private static ArgumentParser newParser() {
    final var parser = ArgumentParsers.newFor(APP_NAME)
                                      .build()
                                      .defaultHelp(true)
                                      .description(APP_DESCRIPTION);

    parser.addArgument("-p", "--" + PROJECT_KEY_ARG_NAME)
          .help("The sonarqube project key")
          .required(true);

    parser.addArgument("--" + PULL_REQUEST_ARG_NAME)
          .help("Pull request to scan, keep empty for a global report")
          .setDefault("")
          .required(false);

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

    parser.addArgument("--" + TEMPLATE_ARG_NAME)
          .help("Template type")
          .choices("github-small")
          .setDefault("github-small")
          .required(false);

    return parser;
  }

  public static Namespace parse(final String[] args, final IntConsumer exitService) {
    final var parser = newParser();
    try {
      return parser.parseArgs(args);
    } catch (final ArgumentParserException e) {
      parser.handleError(e);
      exitService.accept(-1);
      throw new IllegalStateException();
    }
  }
}
