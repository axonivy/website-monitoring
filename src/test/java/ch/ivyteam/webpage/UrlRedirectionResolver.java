package ch.ivyteam.webpage;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class UrlRedirectionResolver
{
  public static String followRedirections(String url)
  {
    try
    {
      HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
      while (isRedirected(connection.getResponseCode()))
      {
        String newUrl = connection.getHeaderField("Location");
        url = newUrl;
        closeHttpUrlConnectionSilently(connection);
        connection = (HttpURLConnection) new URL(url).openConnection();
      }
      closeHttpUrlConnectionSilently(connection);
      return url;
    }
    catch (Exception e)
    {
      throw new RuntimeException(e);
    }
  }

  private static boolean isRedirected(int httpStatusCode)
  {
    return httpStatusCode == HttpURLConnection.HTTP_MOVED_TEMP
            || httpStatusCode == HttpURLConnection.HTTP_MOVED_PERM
            || httpStatusCode == HttpURLConnection.HTTP_SEE_OTHER
            || httpStatusCode == 307
            || httpStatusCode == 308;
  }

  private static void closeHttpUrlConnectionSilently(HttpURLConnection connection)
  {
    try
    {
      if (connection != null && connection.getInputStream() != null)
      {
        connection.getInputStream().close();
      }
    }
    catch (IOException e)
    {
      // silently
    }
  }

}
