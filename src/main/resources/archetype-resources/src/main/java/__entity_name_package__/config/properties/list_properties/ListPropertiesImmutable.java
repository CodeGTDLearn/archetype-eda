package ${package}.${entity_name_package}.config.properties.list_properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

import java.util.Collections;
import java.util.List;


@ConfigurationProperties(prefix = "application.property")
public class ListPropertiesImmutable {

  private final String task;
  private final List<ListObjects> objects;


  @ConstructorBinding
  public ListPropertiesImmutable(String task, List<ListObjects> objects) {

    this.task = task;
    this.objects = objects;
  }


  public String getTask() {

    return task;
  }

  public List<ListObjects> getObjects() {

    return Collections.unmodifiableList(this.objects);
  }

}