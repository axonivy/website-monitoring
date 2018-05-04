package ch.ivyteam.webpage.helper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

class SitemapParser
{
  static Set<String> parseLinks(String xml)
  {
    try
    {
      Set<String> links = new HashSet<>();
      DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
      Document doc = dBuilder.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
      
      NodeList locList = doc.getElementsByTagName("loc");
      for (int index = 0; index < locList.getLength(); index++)
      {
        String link = locList.item(index).getTextContent();
        links.add(link);
      }
      return links;
    }
    catch (ParserConfigurationException | SAXException | IOException ex)
    {
      throw new IllegalStateException(ex);
    }
  }
}
