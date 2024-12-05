package ${package}.${entity_name}.config.properties;


import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ApplicationPropertiesRecordTest {

  @Autowired
  ApplicationPropertiesRecord properties;

  String expectedName;
  String expectedVersion;

  String actualName;
  String actualVersion;

  @BeforeEach
  void setUp() {

    expectedName = "Scaffold Test";
    expectedVersion = "9.9.9";

    actualName = properties.name();
    actualVersion = properties.version();
  }

  @Order(1)
  @Test
  void givenApplicationPropertiesRecord_whenBinding_thenAllFieldsAreSet() {

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
}