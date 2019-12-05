package ch.ivyteam.webpage;

import static org.assertj.core.api.Assertions.assertThat;

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
    HttpAsserter.assertThat(HTTPS + DOMAIN + "api/currentRelease?releaseVersion=6.0.3").bodyContains("latestReleaseVersion", "latestServiceReleaseVersion", "8.0.");
  }
  
  @Test
  public void checkDeadlinks_onHomepage()
  {
    HttpAsserter.assertThat(HTTPS + DOMAIN).hasNoDeadLinks();
  }

  @Test
  public void checkDeadlinks_artifactDownload()
  {
    HttpAsserter.assertThat("https://download.axonivy.com/7.0.4/AxonIvyEngine7.0.4.58124_Windows_x64.zip").exists();
  }

  @Test
  public void checkDeadlinks_permalinkDebianPackage_80()
  {
    var redirectUrl = HttpAsserter.assertThat(HTTPS + DOMAIN + "permalink/8.0/axonivy-engine.deb").redirectsTemporary();
    assertThat(redirectUrl).matches("https:\\/\\/download\\.axonivy\\.com\\/8\\.0\\.\\d+\\/axonivy-engine-8_8\\.0\\..*deb");
  }

  @Test
  public void checkDeadlinks_onDownloadPage()
  {
    HttpAsserter.assertThat(HTTPS + DOMAIN + "download").hasNoDeadLinks();
  }

  @Test
  public void checkDeadlinks_onDesignerGuidePage()
  {
    HttpAsserter.assertThat(HTTPS + DOMAIN + "doc/dev/designer-guide/").hasNoDeadLinks();
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
