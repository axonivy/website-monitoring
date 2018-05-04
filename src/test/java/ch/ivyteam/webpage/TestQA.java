package ch.ivyteam.webpage;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.URL;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Test;

public class TestQA
{
  private static final String DOMAIN = "answers.axonivy.com";
  private static final String ALTERNATE_DOMAIN = "answer.axonivy.com";
  
  @Test
  public void checkOnline()
  {
    String content = getContent("https://" + DOMAIN);
    assertThat(content).contains("Ask a Question");
  }
  
  @Test
  public void redirect_https()
  {
    String content = getContent("http://" + DOMAIN);
    assertThat(content).contains("Ask a Question");
  }
 
  @Test
  public void redirect_alternateDomain_http()
  {
    String content = getContent("http://" + ALTERNATE_DOMAIN);
    assertThat(content).contains("Ask a Question");
  }
  
  @Test
  public void redirect_alternateDomain_https()
  {
    String content = getContent("https://" + ALTERNATE_DOMAIN);
    assertThat(content).contains("Ask a Question");
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
  
}
