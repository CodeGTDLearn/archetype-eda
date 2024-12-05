package ${package}.${entity_name_package}.config.properties.list_properties;

import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.MethodOrderer.*;


@SpringBootTest
@TestMethodOrder(OrderAnnotation.class)
class ListPropertiesRecordTest {

  @Autowired
  ListPropertiesRecord properties;

  String expectedProperty;

  List<String> expectedAllIndex0Properties;
  List<String> expectedAllIndex1Properties;

  List<ListObjects> actualListObjects;

  String actualProperty;

  @BeforeEach
  void setUp() {

    checkPropertiesTotal(properties, 2);
    checkPropertiesTotal(properties.objects().get(0), 2);

    expectedProperty = "Binding Object Properties Test";

    expectedAllIndex0Properties = List.of("Brazil Test", "Canada Test");
    expectedAllIndex1Properties = List.of("Brazilian Real Test", "Canadian Dollar Test");

    actualListObjects = properties.objects();

    actualProperty = properties.task();
  }

  @Order(1)
  @Test
  void givenTaskProperty_whenBindingToRecord_thenPropertyMatchesExpected() {

    assertEquals(expectedProperty, actualProperty);
  }

  @Order(2)
  @Test
  void givenListOfObjects_whenBindingProperties_thenAllFieldsMatchExpectedValues() {

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
  void givenListOfObjects_whenGettingSize_thenHasExpectedNumberOfItems() {

    int expectedTotalObjects = 2;
    assertEquals(expectedTotalObjects, actualListObjects.size());
  }

  @Order(4)
  @Test
  void givenListObjects_whenAddingNewItem_thenThrowsUnmodifiableException() {

    assertThrows(
         UnsupportedOperationException.class,
         () -> actualListObjects.add(new ListObjects("Test", "Test Currency"))
    );
  }

  @Order(5)
  @Test
  void givenProperties_whenCheckingTypes_thenAllInstancesMatchExpectedClasses() {

    assertAll(() -> assertInstanceOf(String.class, actualProperty));

    actualListObjects
         .forEach(object -> assertInstanceOf(ListObjects.class, object));
  }

  private void checkPropertiesTotal(Object properties, int expectedTotal) {

    int actualTotal = properties.getClass().getDeclaredFields().length;
    assertEquals(expectedTotal, actualTotal);
  }
}