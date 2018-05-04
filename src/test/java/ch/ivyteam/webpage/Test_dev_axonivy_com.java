package ch.ivyteam.webpage;

import org.junit.jupiter.api.Test;

import ch.ivyteam.webpage.helper.HttpAsserter;

public class Test_dev_axonivy_com
{
  private static final String HTTPS = "https://";
  private static final String DOMAIN = "developer.axonivy.com/";
  
  @Test
  void checkOnline()
  {
    HttpAsserter.assertThat(HTTPS + DOMAIN).bodyContains("AXON.IVY DIGITAL BUSINESS PLATFORM");
  }
  
  @Test
  void apiCurrentRelease()
  {
    HttpAsserter.assertThat(HTTPS + DOMAIN + "api/currentRelease?releaseVersion=6.0.3").bodyContains("latestReleaseVersion", "latestServiceReleaseVersion", "6.0.");
  }
  
  @Test
  public void checkDeadlinks_onHomepage()
  {
    HttpAsserter.assertThat(HTTPS + DOMAIN ).hasNoDeadLinks();
  }
  
  @Test
  public void checkDeadlinks_onDownloadPage()
  {
    HttpAsserter.assertThat(HTTPS + DOMAIN + "download").hasNoDeadLinks();
  }

  @Test
  public void checkDeadlinks_onAddonsPage()
  {
    HttpAsserter.assertThat(HTTPS + DOMAIN + "download/addons").hasNoDeadLinks();
  }

  @Test
  public void checkDeadlinks_onDesignerGuidePage()
  {
    HttpAsserter.assertThat(HTTPS + DOMAIN + "doc/latest/DesignerGuideHtml/").hasNoDeadLinks();
  }
  
  @Test
  public void checkDeadlinks_onMavenPage()
  {
    HttpAsserter.assertThat(HTTPS + DOMAIN + "download/maven.html").hasNoDeadLinks();
  }
  
  @Test
  public void sitemap()
  {
    HttpAsserter.assertThat(HTTPS + DOMAIN).hasValidSitemap();
  }
}
