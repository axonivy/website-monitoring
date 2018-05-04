package ch.ivyteam.webpage;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class TestDeveloperWebpageLinks
{
  private static final String PRIMEFACES_ORG = "primefaces.org";
  private static final Set<String> EXCLUDE_SITES_WHICH_CONTAINS = new HashSet<>();
  private static final Set<String> DO_NOT_CHECK_LINKS_WHICH_CONTAINS = new HashSet<>();
  static
  {
    // Primefaces blocks the crawling
    EXCLUDE_SITES_WHICH_CONTAINS.add(PRIMEFACES_ORG);
    // Much broken links in PublicAPI because we only provide JavaDoc of classes
    // which are annotated with @PublicAPI
    EXCLUDE_SITES_WHICH_CONTAINS.add("/PublicAPI/");
    
    // Exclude link which points to https://www.axonivy.com
    DO_NOT_CHECK_LINKS_WHICH_CONTAINS.add("327-5th-axon-ivy-developer-day");
    // redirect with 301
    DO_NOT_CHECK_LINKS_WHICH_CONTAINS.add("ivysupport.axonivy.com");
    DO_NOT_CHECK_LINKS_WHICH_CONTAINS.add("axonivyhelp.zendesk.com");
    DO_NOT_CHECK_LINKS_WHICH_CONTAINS.add(PRIMEFACES_ORG);
    DO_NOT_CHECK_LINKS_WHICH_CONTAINS.add("localhost:8080/ivy");
    DO_NOT_CHECK_LINKS_WHICH_CONTAINS.add("localhost:8081/ivy");
    
    DO_NOT_CHECK_LINKS_WHICH_CONTAINS.add("ch.linkedin.com");
    
    
    // fixed with > 6.6 (61467634800000 = new Date(2017, 10, 1).getTime())
    if (new Date().getTime() < 61467634800000L)
    {
      // remove me after 1.10.2017
      DO_NOT_CHECK_LINKS_WHICH_CONTAINS.add("jax-rs-spec.java.net");
    }
  }
  
  private static final String HTML_A_TAG_PATTERN = "(?i)<a([^>]+)>";
  private static final String HTML_A_HREF_TAG_PATTERN = "\\s*(?i)href\\s*=\\s*(\"([^\"]*\")|'[^']*'|([^'\">\\s]+))";
  static final Pattern PATTERN_TAG = Pattern.compile(HTML_A_TAG_PATTERN);
  static final Pattern PATTERN_LINK = Pattern.compile(HTML_A_HREF_TAG_PATTERN);
  
  private static final String ROOT_PAGE = "https://developer.axonivy.com/";
  private static final String DOWNLOAD_PAGE = ROOT_PAGE + "download/";
  private static final String DOCUMENTATION_PAGE = ROOT_PAGE + "doc/latest/DesignerGuideHtml/";
  private static final String SITEMAP_XML = ROOT_PAGE + "sitemap.xml";
  private static final String DOWNLOAD_ADDONS_PAGE = ROOT_PAGE + "download/addons.html";
  private static final String DOWNLOAD_MAVEN_PAGE = ROOT_PAGE + "download/maven.html";
  private static final String DOWNLOAD_COMMUNITY_PAGE = ROOT_PAGE + "download/community.html";

  @Test
  public void testDeveloperLinks()
  {
    assertLinksOnPage(ROOT_PAGE);
  }
  
  @Test
  public void testDownloadLinks()
  {
    assertLinksOnPage(DOWNLOAD_PAGE);
  }
  
  @Test
  public void testDownloadAddOnsLinks()
  {
    assertLinksOnPage(DOWNLOAD_ADDONS_PAGE);
  }

  @Test
  public void testCommunityLinks()
  {
    assertLinksOnPage(DOWNLOAD_COMMUNITY_PAGE);
  }

  @Test
  public void testDocumentationLinks()
  {
    assertLinksOnPage(DOCUMENTATION_PAGE);
  }
  
  @Test
  public void testMavenLinks()
  {
    assertLinksOnPage(DOWNLOAD_MAVEN_PAGE);
  }

  private static void assertLinksOnPage(String url)
  {
    assertUrlExists(url);
    Set<String> links = getAllLinks(url);
    assertThat(links).isNotEmpty();
    assertLinksExists(links, url);
  }

  @Test
  public void testSitemapLinks()
  {
    assertUrlExists(SITEMAP_XML);
    Set<String> sitemapLinks = getSitemapLinks();
    assertLinksExists(sitemapLinks, SITEMAP_XML);
  }
  
  @Test
  public void testSitemapPageLinks()
  {
    assertUrlExists(SITEMAP_XML);
    
    StringBuilder failingMessage = new StringBuilder();
    Set<String> sitemapLinks = getSitemapLinks();
    for (String link : sitemapLinks)
    {
      Set<String> linksOfPage = getAllLinks(link);
      Set<String> failingLinks = getFailingLinks(linksOfPage);
      if (!failingLinks.isEmpty())
      {
        failingMessage.append("Failing links on page: " + link);
        failingMessage.append("\n");
        for (String failingLink : failingLinks)
        {
          failingMessage.append(" - missing page: ");
          failingMessage.append(failingLink);
          failingMessage.append("\n");
        }
      }
    }
    assertThat(failingMessage.toString()).as("Failing link(s) found on a page which is listed in the sitemap.xml:").isEmpty();
  }

  private static Set<String> getSitemapLinks()
  {
    try
    {
      String content = getContent(SITEMAP_XML);
      
      Set<String> links = new HashSet<>();
      DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
      Document doc = dBuilder.parse(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
      
      NodeList locList = doc.getElementsByTagName("loc");
      for (int index = 0; index < locList.getLength(); index++)
      {
        String link = locList.item(index).getTextContent();
        if (!isLinkExcluded(link))
        {
          links.add(link);
        }
      }
      return links;
    }
    catch (ParserConfigurationException | SAXException | IOException ex)
    {
      throw new IllegalStateException(ex);
    }
  }

  private static boolean isLinkExcluded(String link)
  {
    for (String excludedSite : EXCLUDE_SITES_WHICH_CONTAINS)
    {
      if (link.contains(excludedSite))
      {
        return true;
      }
    }
    return false;
  }

  private static void assertLinksExists(Set<String> links, String sourceUrl)
  {
    Set<String> failingLinks = getFailingLinks(links);
    assertThat(failingLinks).as("Failing links on page " + sourceUrl).isEmpty();
  }

  private static Set<String> getFailingLinks(Set<String> links)
  {
    Set<String> failingLinks = new HashSet<>();
    for (String link : links)
    {
      if (!existsUrl(link))
      {
        failingLinks.add(link);
      }
    }
    return failingLinks;
  }

  private static void assertUrlExists(String url)
  {
    assertThat(existsUrl(url)).as("The link "+ url +" is not available").isTrue();
  }

  private static boolean existsUrl(String url)
  {
    try (CloseableHttpClient httpClient = createClient())
    {
      URI uri = new URI(url);
      HttpResponse response = httpClient.execute(new HttpGet(uri));
      boolean isOk = response.getStatusLine().getStatusCode() == 200;
      if (!isOk)
      {
        System.err.println("Failed to GET '"+uri+"'. Request returned status "+response.getStatusLine().getStatusCode());
      }
      return isOk;
    }
    catch (URISyntaxException | IOException ex)
    {
      System.err.println(ex);
      ex.printStackTrace();
      return false;
    }
  }

  private static CloseableHttpClient createClient()
  {
    return HttpClientBuilder.create()
            .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
            .build();
  }

  private static Set<String> getAllLinks(String url)
  {
    url = UrlRedirectionResolver.followRedirections(url);
    
    String baseUrl = url.endsWith("/") ? url : StringUtils.substringBeforeLast(url, "/") + "/";
    String content = getContent(url);
    Set<String> result = new HashSet<>();
    Matcher matcherTag = PATTERN_TAG.matcher(content);
    while (matcherTag.find())
    {
      String href = matcherTag.group(1);
      Matcher matcherLink = PATTERN_LINK.matcher(href);
      while (matcherLink.find())
      {
        String link = matcherLink.group(1);
        link = StringUtils.substringBetween(link, "\"", "\"");
        link = StringUtils.substringBeforeLast(link, "#");
        
        if (ignoreLink(link))
        {
          continue;
        }
        if (link.startsWith("/"))
        {
          link = ROOT_PAGE + link;
        }
        else if (!link.startsWith("http"))
        {
          link = baseUrl + link;
        }
        result.add(link);
      }
    }
    return result;
  }

  private static boolean ignoreLink(String link)
  {
    if (link.startsWith("mailto") 
      || link.startsWith("javascript") 
      || link.startsWith("org.eclipse.ui.window.preferences"))
     {
       return true;
     }
     if (link.isEmpty())
     {
       return true;
     }
     for (String ignoreLink : DO_NOT_CHECK_LINKS_WHICH_CONTAINS)
     {
       if (link.contains(ignoreLink))
       {
         return true;
       }
     }
     return false;
  }

  private static String getContent(String url)
  {
    CloseableHttpClient httpClient = HttpClientBuilder.create().build();
    try
    {
      URI uri = new URL(url).toURI();
      return httpClient.execute(new HttpGet(uri), new BasicResponseHandler());
    }
    catch (URISyntaxException | IOException ex)
    {
      throw new IllegalStateException(ex);
    }
    finally
    {
      IOUtils.closeQuietly(httpClient);
    }
  }
}
