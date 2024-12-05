package ${package}.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties("languages")
public class MapPropertiesClass {

  private Map<String, Language> map_country_language;


  public MapPropertiesClass(Map<String, Language> map_country_language) {

    this.map_country_language = map_country_language;
  }

  public Map<String, Language> getMap_country_language() {

    return map_country_language;
  }
}