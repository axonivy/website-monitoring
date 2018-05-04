package ch.ivyteam.webpage;

import org.junit.jupiter.api.Test;

import ch.ivyteam.webpage.helper.HttpAsserter;

class Test_update_axonivy_com
{
  private static final String HTTP = "http://";
  private static final String HTTPS = "https://";

  private static final String DOMAIN = "update.axonivy.com";

  @Test
  void checkOnline_https()
  {
    HttpAsserter.assertThat(HTTPS + DOMAIN).bodyContains("provides an API to get release information of the awesome");
  }

  @Test
  void checkOnline_http()
  {
    HttpAsserter.assertThat(HTTP + DOMAIN).bodyContains("provides an API to get release information of the awesome");
  }

}
