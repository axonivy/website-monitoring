package ch.ivyteam.webpage;

import static ch.ivyteam.webpage.helper.HttpAsserter.assertThat;

import org.junit.jupiter.api.Test;

class Test_license_order_axonivy_io
{
  private static final String HTTP = "http://";
  private static final String HTTPS = "https://";

  private static final String DOMAIN = "license-order.axonivy.io";

  @Test
  void checkOnline()
  {
    assertThat(HTTPS + DOMAIN).bodyContains("Administration");
  }

  @Test
  void redirect_https()
  {
    assertThat(HTTP + DOMAIN).redirectsPermanentTo(HTTPS + DOMAIN + "/");
  }
}
