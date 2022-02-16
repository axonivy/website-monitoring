package ch.ivyteam.webpage;

import static ch.ivyteam.webpage.helper.HttpAsserter.assertThat;

import org.junit.jupiter.api.Test;

class Test_license_axonivy_com
{
  private static final String HTTP = "http://";
  private static final String HTTPS = "https://";

  private static final String DOMAIN = "license.axonivy.com";

  @Test
  void checkOnline()
  {
    assertThat("https://license.axonivy.com/license-order/faces/view/license-order/index.xhtml").bodyContainsIgnoreCase("License Order Application");
  }

  @Test
  void redirect_https()
  {
    assertThat(HTTP + DOMAIN).redirectsPermanentTo(HTTPS + DOMAIN + "/");
  }
}
