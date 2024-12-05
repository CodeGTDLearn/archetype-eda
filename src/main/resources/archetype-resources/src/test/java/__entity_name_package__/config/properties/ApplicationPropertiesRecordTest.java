package ${package}.${entity_name_package}.config.properties;


import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.MethodOrderer.*;

@SpringBootTest
@TestMethodOrder(OrderAnnotation.class)
class ApplicationPropertiesRecordTest {

  @Autowired
  ApplicationPropertiesRecord properties;

  String expectedName;
  String expectedVersion;

  String actualName;
  String actualVersion;

  @BeforeEach
  void setUp() {
    checkPropertiesTotal(properties, 2);

    expectedName = "${artifactId}";
    expectedVersion = "${version}";

    actualName = properties.name();
    actualVersion = properties.version();
  }

  @Order(1)
  @Test
  void givenApplicationPropertiesRecord_whenBinding_thenAllFieldsAreSetCorrectly() {

    assertEquals(expectedName, actualName);
    assertEquals(expectedVersion, actualVersion);
  }

  @Order(2)
  @Test
  void givenApplicationPropertiesRecord_whenRetrieved_thenVerifyFieldTypes() {

    assertAll(
         () -> assertInstanceOf(String.class, actualName),
         () -> assertInstanceOf(String.class, actualVersion)
    );
  }

  @Order(3)
  @Test
  void givenApplicationPropertiesRecord_whenRetrieved_thenFieldsShouldNotBeNull() {

    assertNotNull(actualName);
    assertNotNull(actualVersion);
  }

  @Order(4)
  @Test
  void givenApplicationPropertiesRecord_whenRetrieved_thenFieldsShouldNotBeEmpty() {

    assertFalse(actualName.isEmpty());
    assertFalse(actualVersion.isEmpty());
  }

  @Order(5)
  @Test
  void givenApplicationProperties_whenBound_thenCheckPropertiesTotal() {

    checkPropertiesTotal(properties, 2);
  }

  private void checkPropertiesTotal(Object properties, int expectedTotal) {

    int actualTotal = properties.getClass().getDeclaredFields().length;

    assertEquals(expectedTotal, actualTotal);
  }
}