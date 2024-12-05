package ${package}.${entity_name}.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
#if(${validated_binding_properties} == "true")
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.NotEmpty;

@Validated
#end
@ConfigurationProperties(prefix = "spring.application")
public record ApplicationPropertiesRecord(

#if(${validated_binding_properties} == "true")
     @NotEmpty(message = "Property can not be empty - immutable")
#end
     String name,

#if(${validated_binding_properties} == "true")
     @Pattern(
          message = "Pattern must be N.N.N format - immutable",
          regexp = "^[0-9]+\\.[0-9]+(\\.[0-9])?$")
#end
     String version

) {

}