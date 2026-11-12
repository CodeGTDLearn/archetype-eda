#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
##
##
## APACHE VELOCITY VARIABLES ___________________________________________________
#set($mapProps = "${enable_map_binding_external_properties}")
#set($listProps = "${enable_list_binding_external_properties}")
#set($bindProps = "${enable_binding_external_properties}")
##
##
package ${package}.${entity_package_name};

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

## CONFIGURATION PROPERTIES - BINDING PROPERTIES _______________________________
#if($bindProps == "true")
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

#set($pathConfigProperties = "${package}.${entity_package_name}.config.properties")
@ConfigurationPropertiesScan(basePackages = "${pathConfigProperties}")
#end
##
## GENERAL SPRING-BOOT ANNOTATIONS _____________________________________________
@SpringBootApplication
public class ApiStarter {

  public static void main(String[] args) {

    SpringApplication.run(ApiStarter.class, args);
  }
}