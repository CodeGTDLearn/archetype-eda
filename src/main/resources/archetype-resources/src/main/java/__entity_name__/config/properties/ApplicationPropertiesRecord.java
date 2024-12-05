package ${package}.config.properties;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "spring.application")
public record ApplicationPropertiesRecord(

     @NotEmpty(message = "Property can not be empty - immutable")
     String name,

     @Pattern(
          message = "Pattern must be N.N.N format - immutable",
          regexp = "^[0-9]+\\.[0-9]+(\\.[0-9])?$")
     String version

) {

}