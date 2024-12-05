package ${package}.config.properties;


import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ApplicationPropertiesTest {

  @Autowired
  ApplicationProperties properties;

  String expectedName;
  String expectedVersion;

  String actualName;
  String actualVersion;

  @BeforeEach
  void setUp() {

    expectedName = "Scaffold Test";
    expectedVersion = "9.9.9";

    actualName = properties.getName();
    actualVersion = properties.getVersion();
  }

  @Order(1)
  @Test
  void givenApplicationProperties_whenBound_thenValuesAreSetCorrectly() {

    assertEquals(expectedName, actualName);
    assertEquals(expectedVersion, actualVersion);
  }

  @Order(2)
  @Test
  void givenApplicationProperties_whenBound_thenTypesAreCorrect() {

    assertAll(
         () -> assertInstanceOf(String.class, actualName),
         () -> assertInstanceOf(String.class, actualVersion)
    );
  }

  @Order(3)
  @Test
  void givenApplicationProperties_whenBound_thenNoNullValues() {

    assertNotNull(actualName);
    assertNotNull(actualVersion);
  }

  @Order(4)
  @Test
  void givenApplicationProperties_whenBound_thenNoEmptyValues() {

    assertFalse(actualName.isEmpty());
    assertFalse(actualVersion.isEmpty());
  }
}