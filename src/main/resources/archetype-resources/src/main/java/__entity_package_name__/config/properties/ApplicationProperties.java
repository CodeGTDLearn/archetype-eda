package ${package}.${entity_package_name}.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
##
##
#if(${enable_binding_external_properties_validation} == "true")
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import org.springframework.validation.annotation.Validated;


@Validated
#end
@Component
@ConfigurationProperties(prefix = "spring.application")
public class ApplicationProperties {

#if(${enable_binding_external_properties_validation} == "true")
  @NotEmpty(message = "Property can not be empty")
#end
  private String name;

#if(${enable_binding_external_properties_validation} == "true")
  @Pattern(
       message = "Pattern must be N.N.N[-QUALIFIER] format",
       regexp = "^[0-9]+\\.[0-9]+(\\.[0-9]+)?([-+][A-Za-z0-9.-]+)?$")
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