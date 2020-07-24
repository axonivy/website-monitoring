package ch.ivyteam.webpage;

import static ch.ivyteam.webpage.helper.HttpAsserter.assertThat;

import org.junit.jupiter.api.Test;

class Test_release_axonivy_com
{
  private static final String HTTP = "http://";
  private static final String HTTPS = "https://";

  private static final String DOMAIN = "release.axonivy.com";

  @Test
  void checkOnline_https()
  {
    assertThat(HTTPS + DOMAIN).bodyContains("Axon.ivy Digital Business Platform");
  }

  @Test
  void checkRedirect_http()
  {
    assertThat(HTTP + DOMAIN).redirectsPermanentTo(HTTPS + DOMAIN + "/");
  }
}
