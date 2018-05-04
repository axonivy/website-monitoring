package ch.ivyteam.webpage;

import org.junit.jupiter.api.Test;

import ch.ivyteam.webpage.helper.HttpAsserter;

class Test_license_order_axonivy_io
{
  private static final String HTTP = "http://";
  private static final String HTTPS = "https://";

  private static final String DOMAIN = "license-order.axonivy.io";

  @Test
  void checkOnline()
  {
    HttpAsserter.assertThat(HTTPS + DOMAIN).bodyContains("Administration");
  }

  @Test
  void redirect_https()
  {
    HttpAsserter.assertThat(HTTP + DOMAIN).redirectsPermanentTo(HTTPS + DOMAIN + "/");
  }

}
