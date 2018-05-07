package ch.ivyteam.webpage;

import org.junit.jupiter.api.Test;

import ch.ivyteam.webpage.helper.HttpAsserter;

class Test_file_axonivy_rocks
{
  private static final String HTTP = "http://";
  private static final String HTTPS = "https://";
  private static final String DOMAIN = "file.axonivy.rocks";

  
  @Test
  void checkOnline()
  {
    HttpAsserter.assertThat(HTTPS + DOMAIN + "/p2/").bodyContains("Axon.ivy Update Sites");
  }

  @Test
  void redirect_home()
  {
    HttpAsserter.assertThat(HTTPS + DOMAIN).redirectsTemporaryTo("/p2/");
  }
  
  @Test
  void redirect_https()
  {
    HttpAsserter.assertThat(HTTP + DOMAIN).redirectsPermanentTo(HTTPS + DOMAIN + "/");
  }
  
}
