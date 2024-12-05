package ${package}.config.properties;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;


@Component
@Validated
@ConfigurationProperties(prefix = "spring.application")
public class ApplicationProperties {

  @NotEmpty(message = "Property can not be empty")
  private String name;

  @Pattern(
       message = "Pattern must be N.N.N format",
       regexp = "^[0-9]+\\.[0-9]+(\\.[0-9])?$")
  private String version;

  public String getName() {

    return name;
  }

  public void setName(String name) {

    this.name = name;
  }

  public String getVersion() {

    return version;
  }

  public void setVersion(String version) {

    this.version = version;
  }
}