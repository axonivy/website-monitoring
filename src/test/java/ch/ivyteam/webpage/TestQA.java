package ch.ivyteam.webpage;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.URL;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.jupiter.api.Test;

public class TestQA
{
  private static final String HTTP = "http://";
  private static final String HTTPS = "https://";

  private static final String DOMAIN = "answers.axonivy.com/";
  private static final String ALTERNATE_DOMAIN = "answer.axonivy.com/";

  @Test
  public void checkOnline()
  {
    String content = getContent(HTTP + DOMAIN);
    assertThat(content).contains("Ask a Question");
  }

  @Test
  public void redirect_https()
  {
    assertTemporaryRedirect(HTTP + DOMAIN, HTTPS + DOMAIN);
  }

  @Test
  public void redirect_alternateDomain_http()
  {
    assertPermanentRedirect(HTTP + ALTERNATE_DOMAIN, HTTP + DOMAIN);
  }

  private static String getContent(String url)
  {
    try (CloseableHttpClient httpClient = HttpClientBuilder.create().build())
    {
      URI uri = new URL(url).toURI();
      return httpClient.execute(new HttpGet(uri), new BasicResponseHandler());
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }
  }

  private static CloseableHttpResponse getResponse(String url)
  {
    try (CloseableHttpClient httpClient = HttpClientBuilder.create().disableRedirectHandling().build())
    {
      URI uri = new URL(url).toURI();
      return httpClient.execute(new HttpGet(uri));
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }
  }

  private static void assertPermanentRedirect(String requestUrl, String redirectUrl)
  {
    assertRedirect(requestUrl, redirectUrl, 301);
  }

  private static void assertTemporaryRedirect(String requestUrl, String redirectUrl)
  {
    assertRedirect(requestUrl, redirectUrl, 302);
  }

  private static void assertRedirect(String requestUrl, String redirectUrl, int statusCode)
  {
    try (CloseableHttpResponse response = getResponse(requestUrl))
    {
      assertThat(response.getStatusLine().getStatusCode()).isEqualTo(statusCode);
      assertThat(response.getFirstHeader("Location").getValue()).isEqualTo(redirectUrl);
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }
  }
}
