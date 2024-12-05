package ${package}.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Component;

#if(${enable_lombok} == "true")
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;


@NoArgsConstructor
@Getter
@Setter
#end
@Component
@ConfigurationProperties(prefix = "spring.application")
public class ApplicationProperties {

  private String name;
  private String version;

#if(${enable_lombok} == "false")
  public String getName() {

    return name;
  }

  public String getVersion() {
    return version;
  }

  public void setName(String name) {

    this.name = name;
  }

  public void setVersion(String version) {

    this.version = version;
  }
#end
}