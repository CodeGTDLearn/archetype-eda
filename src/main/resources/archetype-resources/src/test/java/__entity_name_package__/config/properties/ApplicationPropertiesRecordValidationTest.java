package ${package}.${entity_name_package}.config.properties;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.BindException;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.bind.validation.ValidationBindHandler;
import org.springframework.boot.context.properties.source.ConfigurationPropertySource;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;

import java.util.Map;
import java.util.HashMap;

import static org.junit.jupiter.api.MethodOrderer.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD;

@DirtiesContext(classMode = AFTER_EACH_TEST_METHOD)
@ExtendWith(SpringExtension.class)
@TestMethodOrder(OrderAnnotation.class)
@EnableConfigurationProperties(value = ApplicationPropertiesRecord.class)
@TestPropertySource(properties = { // Hardcoded - reason: Only allow '.properties' or 'xml'
     "spring.application.name=Scaffold Test",
     "spring.application.version=9.9.9"
})
class ApplicationPropertiesRecordValidationTest {


  @Order(1)
  @Test
  void givenValidApplicationProperties_whenBound_thenNoValidationException() {

    Map<String, String> properties = new HashMap<>();

    properties.put("spring.application.name", "MyApp");
    properties.put("spring.application.version", "1.9.9");

    ConfigurationPropertySource source = new MapConfigurationPropertySource(properties);
    var validatorFactoryBean = new LocalValidatorFactoryBean();
    validatorFactoryBean.afterPropertiesSet();

    var binder = new Binder(source);
    var bindHandler = new ValidationBindHandler(validatorFactoryBean);

    assertDoesNotThrow(() ->
                            binder.bind(
                                 "spring.application",
                                 Bindable.of(ApplicationPropertiesRecord.class),
                                 bindHandler
                            )
    );
  }

  @Order(2)
  @Test
  void givenEmptyApplicationName_whenBound_thenValidationExceptionThrown() {

    Map<String, String> properties = new HashMap<>();

    properties.put("spring.application.name", "");
    properties.put("spring.application.version", "1.9.9");

    ConfigurationPropertySource source = new MapConfigurationPropertySource(properties);
    var validatorFactoryBean = new LocalValidatorFactoryBean();
    validatorFactoryBean.afterPropertiesSet();

    var binder = new Binder(source);
    var bindHandler = new ValidationBindHandler(validatorFactoryBean);

    assertThrows(
         BindException.class, () ->
              binder.bind(
                   "spring.application",
                   Bindable.of(ApplicationPropertiesRecord.class),
                   bindHandler
              )
    );
  }


  @Order(3)
  @Test
  void givenInvalidVersionPattern_whenBound_thenValidationExceptionThrown() {

    Map<String, String> props = new HashMap<>();

    props.put("spring.application.name", "MyValidName");
    props.put("spring.application.version", "invalid version");

    ConfigurationPropertySource source = new MapConfigurationPropertySource(props);

    var validatorFactoryBean = new LocalValidatorFactoryBean();
    validatorFactoryBean.afterPropertiesSet();

    var binder = new Binder(source);
    var bindHandler = new ValidationBindHandler(validatorFactoryBean);

    assertThrows(
         BindException.class, () ->
              binder.bind(
                   "spring.application",
                   Bindable.of(ApplicationPropertiesRecord.class),
                   bindHandler
              )
    );
  }
}