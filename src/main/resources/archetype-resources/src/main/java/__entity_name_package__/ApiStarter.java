#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
#set($entity_name_lowercase_folder = $entity_name.toLowerCase())
package ${package}.${entity_name_package};

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

#if(${enable_binding_properties} == "true" && (${list_binding_properties} == "true" || ${map_binding_properties} == "true"))
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

#set($pathConfigProperties = "${package}.${entity_name_package}.config.properties")
@ConfigurationPropertiesScan(basePackages = "${pathConfigProperties}")
#end
@SpringBootApplication
public class ApiStarter {

  public static void main(String[] args) {

    SpringApplication.run(ApiStarter.class, args);

    System.out.println("DEBUGXXX");
  }
}