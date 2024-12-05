#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
##
##
## APACHE VELOCITY VARIABLES ___________________________________________________
#set($mapProps = "${map_binding_properties}")
#set($listProps = "${list_binding_properties}")
#set($bindProps = "${enable_binding_properties}")
##
##
package ${package}.${entity_name_package};

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

## CONFIGURATION PROPERTIES - BINDING PROPERTIES _______________________________
#if($bindProps == "true")
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

#set($pathConfigProperties = "${package}.${entity_name_package}.config.properties")
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