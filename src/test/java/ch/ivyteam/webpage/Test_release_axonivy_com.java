package ch.ivyteam.webpage;

import org.junit.jupiter.api.Test;

import ch.ivyteam.webpage.helper.HttpAsserter;

class Test_release_axonivy_com
{
  private static final String HTTP = "http://";
  private static final String HTTPS = "https://";

  private static final String DOMAIN = "release.axonivy.com";

  @Test
  void checkOnline_https()
  {
    HttpAsserter.assertThat(HTTPS + DOMAIN).bodyContains("Axon.ivy Digital Business Platform");
  }

  @Test
  void checkRedirect_http()
  {
    HttpAsserter.assertThat(HTTP + DOMAIN).redirectsPermanentTo(HTTPS + DOMAIN + "/");
  }

}
