package dev.danvega.props.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

import java.util.Map;


@ConfigurationProperties("languages")
public class MapPropertiesImmutableClass {

  private final Map<String, Language> map_country_language;


  @ConstructorBinding
  public MapPropertiesImmutableClass(Map<String, Language> map_country_language) {

    this.map_country_language = map_country_language;
  }

  public Map<String, Language> getMap_country_language() {

    return map_country_language;
  }


}