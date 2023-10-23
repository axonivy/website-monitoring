package ch.ivyteam.webpage;

import static ch.ivyteam.webpage.helper.HttpAsserter.assertThat;

import org.junit.jupiter.api.Test;

class Test_answers_axonivy_com {

  private static final String HTTP = "http://";
  private static final String HTTPS = "https://";
  private static final String DOMAIN = "answers.axonivy.com/";
  private static final String ALTERNATE_DOMAIN = "answer.axonivy.com/";

  @Test
  void checkOnline() {
    assertThat(HTTPS + DOMAIN).bodyContains("Ask a Question");
  }

  @Test
  void redirect_https() {
    assertThat(HTTP + DOMAIN).redirectsTemporaryTo(HTTPS + DOMAIN);
  }

  @Test
  void redirect_alternateDomain_http() {
    assertThat(HTTP + ALTERNATE_DOMAIN).redirectsPermanentTo(HTTP + DOMAIN);
  }
}
