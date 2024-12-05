#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package};

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

#if(${enable_binding_properties} == "true" &&
   (${list_binding_properties} == "true" ||
    ${map_binding_properties} == "true"))
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;


@ConfigurationPropertiesScan(basePackages = "${package}.config.properties")
#end
@SpringBootApplication
public class ApiStarter {

  public static void main(String[] args) {

    SpringApplication.run(ApiStarter.class, args);
  }

}