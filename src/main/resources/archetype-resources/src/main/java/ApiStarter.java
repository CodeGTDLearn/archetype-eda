#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package};

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

//@EnableConfigurationProperties({
//     ListPropertiesClass.class,
//     ListPropertiesRecord.class,
//     MapPropertiesClass.class,
//     MapPropertiesRecord.class
//})
@ConfigurationPropertiesScan(basePackages = "dev.danvega.props.properties")


@SpringBootApplication
public class ApiStarter {

  public static void main(String[] args) {

    SpringApplication.run(ApiStarter.class, args);
  }

}