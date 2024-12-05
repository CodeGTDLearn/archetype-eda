package ${package}.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
#if(${validated_binding_properties} == "true")
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import org.springframework.validation.annotation.Validated;


@Validated
#end
@Component
@ConfigurationProperties(prefix = "spring.application")
public class ApplicationProperties {

#if(${validated_binding_properties} == "true")
  @NotEmpty(message = "Property can not be empty")
#end
  private String name;

#if(${validated_binding_properties} == "true")
  @Pattern(
       message = "Pattern must be N.N.N format",
       regexp = "^[0-9]+\\.[0-9]+(\\.[0-9])?$")
#end
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