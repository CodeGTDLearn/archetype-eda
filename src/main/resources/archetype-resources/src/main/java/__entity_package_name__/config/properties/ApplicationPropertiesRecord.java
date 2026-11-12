package ${package}.${entity_package_name}.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
#if(${enable_binding_external_properties_validation} == "true")
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.NotEmpty;

@Validated
#end
@ConfigurationProperties(prefix = "spring.application")
public record ApplicationPropertiesRecord(

#if(${enable_binding_external_properties_validation} == "true")
     @NotEmpty(message = "Property can not be empty - immutable")
#end
     String name,

#if(${enable_binding_external_properties_validation} == "true")
     @Pattern(
          message = "Pattern must be N.N.N[-QUALIFIER] format - immutable",
          regexp = "^[0-9]+\\.[0-9]+(\\.[0-9]+)?([-+][A-Za-z0-9.-]+)?$")
#end
     String version

) {

}