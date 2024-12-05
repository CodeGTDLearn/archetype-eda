package dev.danvega.props.list;


import dev.danvega.props.properties.ApplicationProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ApplicationPropertiesTest {

  @Autowired
  private ApplicationProperties applicationProperties;

  String expectedName;
  String expectedVersion;

  String actualName;
  String actualVersion;


  @BeforeEach
  void setUp() {

    expectedName = "Scaffold";
    expectedVersion = "1.0.0";

    actualName = applicationProperties.getName();
    actualVersion = applicationProperties.getVersion();
  }


  @Test
  void check_properties() {

    assertEquals(expectedName, actualName);
    assertEquals(expectedVersion, actualVersion);
  }

  @Test
  void shouldNotGet_properties_withNullValue() {

    assertNotNull(actualName);
    assertNotNull(actualVersion);
  }

  @Test
  void shouldNotGet_properties_withEmptyValue() {

    assertFalse(actualName.isEmpty());
    assertFalse(actualVersion.isEmpty());
  }
}