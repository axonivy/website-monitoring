package ch.ivyteam.webpage;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import ch.ivyteam.webpage.helper.HttpAsserter;

class Test_dev_axonivy_com {

  private static final String HTTPS = "https://";
  private static final String DOMAIN = "developer.axonivy.com/";

  @Test
  void checkOnline() {
    HttpAsserter.assertThat(HTTPS + DOMAIN).bodyContainsIgnoreCase("ivy");
  }

  @Test
  void apiStatus() {
    HttpAsserter.assertThat(HTTPS + DOMAIN + "api/status").bodyContains("phpVersion");
  }

  @Test
  void apiCurrentRelease() {
    HttpAsserter.assertThat(HTTPS + DOMAIN + "api/currentRelease?releaseVersion=6.0.3")
            .bodyContains("latestReleaseVersion", "latestServiceReleaseVersion");
  }

  @Test
  void checkDeadlinks_onHomepage() {
    HttpAsserter.assertThat(HTTPS + DOMAIN).hasNoDeadLinks();
  }

  @Test
  void checkDeadlinks_artifactDownload() {
    HttpAsserter.assertThat("https://download.axonivy.com/7.0.4/AxonIvyEngine7.0.4.58124_Windows_x64.zip")
            .exists();
  }

  @Test
  void checkDeadlinks_permalinkDebianPackage_80() {
    var redirectUrl = HttpAsserter.assertThat(HTTPS + DOMAIN + "permalink/8.0/axonivy-engine.deb")
            .redirectsTemporary();
    assertThat(redirectUrl)
            .matches("https:\\/\\/download\\.axonivy\\.com\\/8\\.0\\.\\d+\\/axonivy-engine-8_8\\.0\\..*deb");
  }

  @Test
  void checkDeadlinks_onDownloadPage() {
    HttpAsserter.assertThat(HTTPS + DOMAIN + "download").hasNoDeadLinks();
  }

  @Test
  void checkDeadlinks_onDesignerGuidePage() {
    HttpAsserter.assertThat(HTTPS + DOMAIN + "doc/dev/designer-guide/").hasNoDeadLinks();
  }

  @Test
  void checkDeadlinks_onMavenPage() {
    HttpAsserter.assertThat(HTTPS + DOMAIN + "download/maven.html").hasNoDeadLinks();
  }

  @Test
  void checkDeadlinks_onDoc() {
    HttpAsserter.assertThat(HTTPS + DOMAIN + "doc").hasNoDeadLinks();
  }

  @Test
  void checkDeadlinks_onNews() {
    HttpAsserter.assertThat(HTTPS + DOMAIN + "news").hasNoDeadLinks();
  }

  @Test
  void sitemap() {
    HttpAsserter.assertThat(HTTPS + DOMAIN).hasValidSitemap();
  }
}
