package ${package}.${entity_name}.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "application.property")
public class ListProperties {

  private String task;
  private List<ListObjects> objects;

  public void setTask(String task) {

    this.task = task;
  }

  public void setObjects(List<ListObjects> objects) {

    this.objects = objects;
  }

  public String getTask() {

    return task;
  }

  public List<ListObjects> getObjects() {

    return Collections.unmodifiableList(this.objects);
  }
}