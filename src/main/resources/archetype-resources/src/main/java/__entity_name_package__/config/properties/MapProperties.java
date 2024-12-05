package ${package}.${entity_name_package}.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;

@Component
@ConfigurationProperties("application.property")
public class MapProperties {

  private Map<String, MapItem> map;

  public MapProperties(Map<String, MapItem> map) {

    this.map = map;
  }

  public void setMap(Map<String, MapItem> map) {

    this.map = map;
  }

  public Map<String, MapItem> getMap() {

    return Collections.unmodifiableMap(this.map);
  }
}