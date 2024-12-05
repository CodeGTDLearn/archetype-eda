package ${package}.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Collections;
import java.util.Map;

@ConfigurationProperties("application.property")
public record MapPropertiesRecord(
     Map<String, MapItem> map
) {

  @Override
  public Map<String, MapItem> map() {

    return Collections.unmodifiableMap(this.map);
  }
}