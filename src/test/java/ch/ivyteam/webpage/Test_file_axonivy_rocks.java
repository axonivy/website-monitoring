package ch.ivyteam.webpage;

import static ch.ivyteam.webpage.helper.HttpAsserter.assertThat;

import org.junit.jupiter.api.Test;

class Test_file_axonivy_rocks
{
  private static final String HTTP = "http://";
  private static final String HTTPS = "https://";
  private static final String DOMAIN = "file.axonivy.rocks";

  @Test
  void checkOnline()
  {
    assertThat(HTTPS + DOMAIN + "/p2/").bodyContains("Update Sites");
  }

  @Test
  void redirect_home()
  {
    assertThat(HTTPS + DOMAIN).redirectsTemporaryTo("/p2/");
  }
  
  @Test
  void redirect_https()
  {
    assertThat(HTTP + DOMAIN).redirectsPermanentTo(HTTPS + DOMAIN + "/");
  }
}
