package ch.ivyteam.webpage.helper;

import java.net.URI;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.assertj.core.api.Assertions;

import jdk.incubator.http.HttpClient;
import jdk.incubator.http.HttpClient.Redirect;
import jdk.incubator.http.HttpRequest;
import jdk.incubator.http.HttpRequest.BodyPublisher;
import jdk.incubator.http.HttpResponse;
import jdk.incubator.http.HttpResponse.BodyHandler;

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

    public void bodyContains(String ... substringOfBody)
    {
      var content = getContent(url);
      Assertions.assertThat(content).contains(substringOfBody);
    }

    private static HttpResponse<String> getResponse(String url)
    {
    	return getResponse(url, false);
    }
    
    private static HttpResponse<String> getResponseFollowRedirects(String url)
    {
    	return getResponse(url, true);
    }

    private static String getContent(String url) {
    	try {
    		System.out.println("Crawling (GET): " + url);
    		var client = HttpClient.newBuilder().followRedirects(Redirect.NEVER).build();
			var request = HttpRequest.newBuilder().uri(URI.create(url)).build();
			var response = client.send(request, BodyHandler.asString());
			return response.body();
		} catch (Exception ex) {
			throw new RuntimeException("Could not crawl: " + url, ex);
		}
	}
    
    private static HttpResponse<String> getResponse(String url, boolean followRedirects)
    {
    	try {
    		var method = "HEAD";
    		if (url.contains("developer.axonivy.com")) {
    			method = "GET";
    		}
    		System.out.println("Crawling ("+method+" - Drop Body): " + url);
    		
    		var redirectPolicy = followRedirects ? Redirect.ALWAYS : Redirect.NEVER;
    		var client = HttpClient.newBuilder().followRedirects(redirectPolicy).build();
			var request = HttpRequest.newBuilder()
					.method(method, BodyPublisher.noBody())
					.uri(URI.create(url))
					.build();
			return client.send(request, BodyHandler.discard(""));
		} catch (Exception ex) {
			throw new RuntimeException("Could not crawl: " + url, ex);
		}
    }
		

    
    
    
    
    
    
    
    
    
    
    private static final Set<String> DO_NOT_CHECK_LINKS_WHICH_CONTAINS = Set.of(
    		"PublicAPI",
    		"primefaces.org",
    		"ch.linkedin.com");
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
