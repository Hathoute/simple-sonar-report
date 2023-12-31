package com.hathoute.sonarqube.report;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Map;
import java.util.function.IntConsumer;
import java.util.stream.Stream;

import static com.hathoute.sonarqube.report.ArgumentParserUtil.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class ArgumentParserUtilTest {

  private IntConsumer exitService;

  @BeforeEach
  void setup() {
    exitService = mock(IntConsumer.class);
  }

  @AfterEach
  void teardown() {
    verifyNoMoreInteractions(exitService);
  }

  @Test
  void should_parse_minimal_command_line() {
    var commandLine = "-p projectName -u https://test.com -t privatetoken";
    var args = commandLine.split(" ");

    var actual = ArgumentParserUtil.parse(args, exitService);

    assertThat(actual.getString(PROJECT_KEY_ARG_NAME)).isEqualTo("projectName");
    assertThat(actual.getString(URL_ARG_NAME)).isEqualTo("https://test.com");
    assertThat(actual.getString(TOKEN_ARG_NAME)).isEqualTo("privatetoken");
    assertThat(actual.getString(OUTPUT_ARG_NAME)).isNotBlank();
    assertThat(actual.getString(TEMPLATE_ARG_NAME)).isNotBlank();
    assertThat(actual.getString(PULL_REQUEST_ARG_NAME)).isBlank();
  }

  @Test
  void should_parse_full_command_line() {
    var commandLine = "-p projectName -u https://test.com -t privatetoken -o output.md --template github-small --pullrequest 69";
    var args = commandLine.split(" ");

    var actual = ArgumentParserUtil.parse(args, exitService);

    assertThat(actual.getString(PROJECT_KEY_ARG_NAME)).isEqualTo("projectName");
    assertThat(actual.getString(URL_ARG_NAME)).isEqualTo("https://test.com");
    assertThat(actual.getString(TOKEN_ARG_NAME)).isEqualTo("privatetoken");
    assertThat(actual.getString(OUTPUT_ARG_NAME)).isEqualTo("output.md");
    assertThat(actual.getString(TEMPLATE_ARG_NAME)).isEqualTo("github-small");
    assertThat(actual.getString(PULL_REQUEST_ARG_NAME)).isEqualTo("69");
  }

  @Test
  void should_parse_command_line_with_long_names() {
    var commandLine = "--projectkey projectName --url https://test.com --token privatetoken --output output.md " +
        "--template github-small --pullrequest 69";
    var args = commandLine.split(" ");

    var actual = ArgumentParserUtil.parse(args, exitService);

    assertThat(actual.getString(PROJECT_KEY_ARG_NAME)).isEqualTo("projectName");
    assertThat(actual.getString(URL_ARG_NAME)).isEqualTo("https://test.com");
    assertThat(actual.getString(TOKEN_ARG_NAME)).isEqualTo("privatetoken");
    assertThat(actual.getString(OUTPUT_ARG_NAME)).isEqualTo("output.md");
    assertThat(actual.getString(TEMPLATE_ARG_NAME)).isEqualTo("github-small");
    assertThat(actual.getString(PULL_REQUEST_ARG_NAME)).isEqualTo("69");
  }

  @ParameterizedTest
  @CsvSource(value = {"-p", "-u", "-t"})
  void should_fail_when_required_parameter_not_provided(String excludedParam) {
    var mapArgs = Map.of("-p", "projectName", "-u", "https://test.com", "-t", "privatetoken");
    var args = mapArgs.entrySet().stream()
        .filter(e -> !e.getKey().equals(excludedParam))
        .flatMap(e -> Stream.of(e.getKey(), e.getValue())).toArray(String[]::new);

    assertThatThrownBy(() -> ArgumentParserUtil.parse(args, exitService)).isInstanceOf(IllegalStateException.class);

    verify(exitService).accept(-1);
  }

  @Test
  void should_fail_on_invalid_template_choice() {
    var commandLine = "-p projectName -u https://test.com -t privatetoken --template invalid-choice";
    var args = commandLine.split(" ");

    assertThatThrownBy(() -> ArgumentParserUtil.parse(args, exitService)).isInstanceOf(IllegalStateException.class);

    verify(exitService).accept(-1);
  }

  @Test
  void should_fail_on_invalid_argument() {
    var commandLine = "-p projectName -u https://test.com -t privatetoken --invalidparam value";
    var args = commandLine.split(" ");

    assertThatThrownBy(() -> ArgumentParserUtil.parse(args, exitService)).isInstanceOf(IllegalStateException.class);

    verify(exitService).accept(-1);
  }
}