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
    assertThat(HTTPS + DOMAIN).bodyContains("https://license-order.axonivy.io/license-order/faces/view/license-order/businesspartner-order.xhtml");
  }

  @Test
  void redirect_https()
  {
    assertThat(HTTP + DOMAIN).redirectsPermanentTo(HTTPS + DOMAIN + "/");
  }
}
