package ch.ivyteam.webpage.helper;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;

class SitemapParser {

  static Set<String> parseLinks(String xml) {
    try {
      var links = new HashSet<String>();
      var dbFactory = DocumentBuilderFactory.newInstance();
      var dBuilder = dbFactory.newDocumentBuilder();
      var doc = dBuilder.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
      var locList = doc.getElementsByTagName("loc");
      for (var index = 0; index < locList.getLength(); index++) {
        var link = locList.item(index).getTextContent();
        links.add(link);
      }
      return links;
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }
}
