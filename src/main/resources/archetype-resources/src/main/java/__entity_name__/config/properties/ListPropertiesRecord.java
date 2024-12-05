package ${package}.${entity_name}.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Collections;
import java.util.List;

@ConfigurationProperties(prefix = "application.property")
public record ListPropertiesRecord(
     String task,
     List<ListObjects> objects
) {

  @Override
  public String task() {

    return task;
  }

  @Override
  public List<ListObjects> objects() {

    return Collections.unmodifiableList(this.objects);
  }
}