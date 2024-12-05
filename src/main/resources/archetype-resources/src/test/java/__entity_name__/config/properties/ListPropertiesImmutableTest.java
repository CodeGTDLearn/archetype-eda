package ${package}.${entity_name}.config.properties;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ListPropertiesImmutableTest {

  @Autowired
  ListPropertiesImmutable properties;

  String expectedProperty;

  List<String> expectedAllIndex0Properties;
  List<String> expectedAllIndex1Properties;

  List<ListObjects> actualListObjects;

  String actualProperty;

  @BeforeEach
  void setUp() {

    expectedProperty = "Binding Object Properties Test";

    expectedAllIndex0Properties = List.of("Brazil Test", "Canada Test");
    expectedAllIndex1Properties = List.of("Brazilian Real Test", "Canadian Dollar Test");

    actualListObjects = properties.getObjects();

    actualProperty = properties.getTask();
  }

  @Order(1)
  @Test
  void givenPropertiesFile_whenBindingTaskProperty_thenValueIsSet() {

    assertEquals(expectedProperty, actualProperty);
  }

  @Order(2)
  @Test
  void givenPropertiesFile_whenBindingListObjects_thenAllFieldsAreSet() {

    IntStream
         .range(0, actualListObjects.size())
         .forEach(index -> {

           var objectProperty1 = expectedAllIndex0Properties.get(index);
           var objectProperty2 = expectedAllIndex1Properties.get(index);

           var actualObject = actualListObjects.get(index);

           assertAll("List Property - Index: " + index,
                     () -> assertEquals(objectProperty1, actualObject.name()),
                     () -> assertEquals(objectProperty2, actualObject.currency())
           );
         });
  }

  @Order(3)
  @Test
  void givenPropertiesFile_whenBindingListObjects_thenCorrectSizeIsSet() {

    int expectedTotalObjects = 2;

    assertEquals(expectedTotalObjects, actualListObjects.size());
  }

  @Order(4)
  @Test
  void givenPropertiesFile_whenAccessingListObjects_thenListIsImmutable() {

    assertThrows(
         UnsupportedOperationException.class,
         () -> actualListObjects.add(new ListObjects("Test", "Test Currency"))
    );
  }

  @Order(5)
  @Test
  void givenPropertiesFile_whenBindingProperties_thenVerifyTypes() {

    assertAll(() -> assertInstanceOf(String.class, actualProperty));

    actualListObjects
         .forEach(object -> assertInstanceOf(ListObjects.class, object));
  }
}