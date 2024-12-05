package dev.danvega.props.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@ConfigurationProperties("languages")
public class MapPropertiesClass {

  private Map<String, Language> mapCountryLanguage;


  public MapPropertiesClass(Map<String, Language> mapCountryLanguage) {

    this.mapCountryLanguage = mapCountryLanguage;
  }

  public void setMapCountryLanguage(Map<String, Language> mapCountryLanguage) {

    this.mapCountryLanguage = mapCountryLanguage;
  }

  public Map<String, Language> getMapCountryLanguage() {

    return mapCountryLanguage;
  }


}