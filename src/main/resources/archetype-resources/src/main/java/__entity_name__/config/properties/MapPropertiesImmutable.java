package ${package}.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

import java.util.Collections;
import java.util.Map;


@ConfigurationProperties("application.property")
public class MapPropertiesImmutable {

  private final Map<String, MapItem> map;

  @ConstructorBinding
  public MapPropertiesImmutable(Map<String, MapItem> map) {

    this.map = map;
  }

  public Map<String, MapItem> getMap() {

    return Collections.unmodifiableMap(this.map);
  }
}