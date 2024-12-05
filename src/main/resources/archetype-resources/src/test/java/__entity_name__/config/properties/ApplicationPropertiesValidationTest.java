package ${package}.${entity_name}.config.properties;


import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.bind.BindException;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.bind.validation.ValidationBindHandler;
import org.springframework.boot.context.properties.source.ConfigurationPropertySource;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD;

@DirtiesContext(classMode = AFTER_EACH_TEST_METHOD)
@ExtendWith(SpringExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@EnableConfigurationProperties(value = ApplicationProperties.class)
@TestPropertySource(properties = { // Hardcoded - reason: Only allow '.properties' or 'xml'
     "spring.application.name=Scaffold Test",
     "spring.application.version=9.9.9"
})
class ApplicationPropertiesValidationTest {

  @Autowired
  ApplicationProperties properties;

  private static Validator validator;

  @BeforeAll
  public static void setup() {

    validator = Validation.buildDefaultValidatorFactory().getValidator();
  }

  @Order(1)
  @Test
  void givenValidProperties_whenBound_thenNoValidationExceptionThrown() {

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
                                 Bindable.of(ApplicationProperties.class),
                                 bindHandler
                            )
    );
  }

  @Order(1)
  @Test
  void givenEmptyNameProperty_whenBound_thenValidationExceptionThrown() {

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
                   Bindable.of(ApplicationProperties.class),
                   bindHandler
              )
    );
  }

  @Order(2)
  @Test
  void givenInvalidVersionFormat_whenBound_thenValidationExceptionThrown() {

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
                   Bindable.of(ApplicationProperties.class),
                   bindHandler
              )
    );
  }

  @Order(3)
  @Test
  void givenEmptyNameProperty_whenValidated_thenShowNotEmptyMessage() {

    properties.setName("");

    Set<ConstraintViolation<ApplicationProperties>> violations =
         validator.validate(properties);

    assertFalse(violations.isEmpty());

    assertTrue(
         violations
              .stream()
              .anyMatch(
                   violation -> {
                     var message = violation.getMessage();
                     return message.equals("Property can not be empty");
                   }
              )
    );
  }

  @Order(4)
  @Test
  void givenInvalidVersionFormat_whenValidated_thenShowPatternMessage() {

    properties.setVersion("invalid-version");

    Set<ConstraintViolation<ApplicationProperties>> violations =
         validator.validate(properties);

    assertFalse(violations.isEmpty());

    assertTrue(
         violations
              .stream()
              .anyMatch(
                   violation -> {
                     var message = violation.getMessage();
                     return message.equals("Pattern must be N.N.N format");
                   }
              )
    );
  }
}