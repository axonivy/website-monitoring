package ch.ivyteam.webpage.helper;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.assertj.core.api.Assertions;

public class HttpAsserter
{
  public static HttpAssert assertThat(String url)
  {
    return new HttpAssert(url);
  }

  public static class HttpAssert
  {
    private String url;

    private HttpAssert(String url)
    {
      this.url = url;
    }

    public void redirectsTemporaryTo(String redirectUrl)
    {
      assertRedirect(url, redirectUrl, 302);
    }

    public void redirectsPermanentTo(String redirectUrl)
    {
      assertRedirect(url, redirectUrl, 301);
    }

    public void exists()
    {
      Assertions.assertThat(getResponse(url).statusCode()).isEqualTo(200);
    }

    private static void assertRedirect(String requestUrl, String redirectUrl, int statusCode)
    {
      var response = getResponse(requestUrl);
      Assertions.assertThat(response.statusCode()).isEqualTo(statusCode);
      Assertions.assertThat(response.headers().firstValue("Location").get()).isEqualTo(redirectUrl);
    }

    public String redirectsTemporary()
    {
      var response = getResponse(url);
      Assertions.assertThat(response.statusCode()).isEqualTo(302);
      return response.headers().firstValue("Location").get();
    }

    public void bodyContains(String ... substringOfBody)
    {
      var content = getContent(url);
      Assertions.assertThat(content).contains(substringOfBody);
    }

    public void bodyContainsIgnoreCase(String substringOfBody)
    {
      var content = getContent(url);
      Assertions.assertThat(content).containsIgnoringCase(substringOfBody);
    }

    private static HttpResponse<Void> getResponse(String url)
    {
    	return getResponse(url, false);
    }

    private static HttpResponse<Void> getResponseFollowRedirects(String url)
    {
    	return getResponse(url, true);
    }

    private static String getContent(String url)
    {
      System.out.println("Crawling (GET): " + url);
      var client = HttpClient.newBuilder().followRedirects(Redirect.NEVER).build();
      var request = HttpRequest.newBuilder().uri(URI.create(url)).build();
      try
      {
        return retryHandler(client, request, BodyHandlers.ofString()).body();
      }
      catch (Exception ex)
      {
        throw new RuntimeException("Could not crawl: " + url, ex);
      }
    }

    private static HttpResponse<Void> getResponse(String url, boolean followRedirects)
    {
      var method = "HEAD";
      if (url.contains("developer.axonivy.com") || url.contains("file.axonivy.rocks"))
      {
        method = "GET";
      }
      System.out.println("Crawling (" + method + " - Drop Body): " + url);

      var redirectPolicy = followRedirects ? Redirect.ALWAYS : Redirect.NEVER;
      var client = HttpClient.newBuilder().followRedirects(redirectPolicy).build();
      var request = HttpRequest.newBuilder()
              .method(method, BodyPublishers.noBody())
              .uri(URI.create(url))
              .header("User-Agent", "Firefox/84")
              .build();
      try
      {
        return retryHandler(client, request, BodyHandlers.discarding());
      }
      catch (Exception ex)
      {
        throw new RuntimeException("Could not crawl: " + url, ex);
      }
    }

    private static <T> HttpResponse<T> retryHandler(HttpClient client, HttpRequest request, BodyHandler<T> bodyHandler)
            throws InterruptedException
    {
      var retry = 3;
      Throwable exception = null;
      while (retry > 0)
      try
      {
        return client.send(request, bodyHandler);
      }
      catch (IOException ex)
      {
        retry--;
        exception = ex;
      }
      throw new RuntimeException("Failed at least 3 times: ", exception);
    }

    private static final Set<String> DO_NOT_CHECK_LINKS_WHICH_CONTAINS = Set.of(
    		"PublicAPI",
    		"primefaces.org",
    		"jira.axonivy.com",
    		"ch.linkedin.com",
    		"portal-guide/index.html");
    private static final Set<String> DO_NOT_CHECK_LINK_WHICH_STARTS_WITH = Set.of(
    		"mailto",
    		"javascript",
    		"org.eclipse.ui.window.preferences");

    public void hasNoDeadLinks()
    {
      assertThat(url).exists();

      Set<String> links = parseAllLinksOfPage(url);
      Assertions.assertThat(links).isNotEmpty();
      System.out.println("Found " + links.size() + " links on page " + url);

      Set<String> failingLinks = getDeadLinks(links);
      Assertions.assertThat(failingLinks).as("Found dead links on " + url).isEmpty();
    }

    private static Set<String> getDeadLinks(Set<String> links)
    {
      return links.stream()
              .filter(link -> getResponseFollowRedirects(link).statusCode() != 200)
              .collect(Collectors.toSet());
    }

    private static Set<String> parseAllLinksOfPage(String baseUrl)
    {
      final var PATTERN_TAG = Pattern.compile("(?i)<a([^>]+)>");
      final var PATTERN_LINK = Pattern.compile("\\s*(?i)href\\s*=\\s*(\"([^\"]*\")|'[^']*'|([^'\">\\s]+))");

      var content = getContent(baseUrl);
      var result = new HashSet<String>();
      var matcherTag = PATTERN_TAG.matcher(content);
      while (matcherTag.find())
      {
        var href = matcherTag.group(1);
        var matcherLink = PATTERN_LINK.matcher(href);
        while (matcherLink.find())
        {
          var link = matcherLink.group(1);
          link = StringUtils.substringBetween(link, "\"", "\"");
          link = StringUtils.substringBeforeLast(link, "#");

          if (ignoreLink(link))
          {
            continue;
          }
          else if (!link.startsWith("http"))
          {
            if (link.startsWith("/")) // root relative
            {
              link = getProtocolAndHost(baseUrl) + link;
            }
            else // relative from current site
            {
              link = StringUtils.removeEnd(baseUrl, "/") + "/" + StringUtils.removeStart(link, "/");
            }
          }
          result.add(link);
        }
      }
      return result;
    }

    private static String getProtocolAndHost(String url)
    {
      try
      {
        var javaURL = new URL(url);
        return javaURL.getProtocol() + "://" + javaURL.getHost();
      }
      catch (Exception ex)
      {
        throw new RuntimeException(ex);
      }
    }

	private static boolean ignoreLink(String link) {
		if (link.isEmpty()) {
			return true;
		}
		if (DO_NOT_CHECK_LINK_WHICH_STARTS_WITH.stream().anyMatch(pattern -> link.startsWith(pattern))) {
			return true;
		}

		if (DO_NOT_CHECK_LINKS_WHICH_CONTAINS.stream().anyMatch(pattern -> link.contains(pattern))) {
			return true;
		}

		return false;
	}

    public void hasValidSitemap()
    {
      String sitemapUrl = url + "sitemap.xml";
      HttpAsserter.assertThat(sitemapUrl).exists();
      String xml = getContent(sitemapUrl);
      Set<String> sitemapLinks = SitemapParser.parseLinks(xml);
      sitemapLinks = sitemapLinks.stream().filter(l -> !ignoreLink(l)).collect(Collectors.toSet());

      Assertions.assertThat(sitemapLinks).isNotEmpty();
      System.out.println("Found " + sitemapLinks.size() + " links sitemap " + url);

      Set<String> failingLinks = getDeadLinks(sitemapLinks);
      Assertions.assertThat(failingLinks).as("Found dead links on " + url).isEmpty();
    }
  }
}
