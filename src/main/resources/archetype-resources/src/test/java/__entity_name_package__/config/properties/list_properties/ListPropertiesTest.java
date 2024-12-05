package ${package}.${entity_name_package}.config.properties.list_properties;

import org.junit.jupiter.api.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.MethodOrderer.*;

@SpringBootTest
@TestMethodOrder(OrderAnnotation.class)
class ListPropertiesTest {

  @Autowired
  ListProperties properties;

  String expectedProperty;

  List<String> expectedAllIndex0Properties;
  List<String> expectedAllIndex1Properties;

  List<ListObjects> actualListObjects;

  String actualProperty;

  @BeforeEach
  void setUp() {

    checkPropertiesTotal(properties, 2);
    checkPropertiesTotal(properties.getObjects().get(0), 2);

    expectedProperty = "Binding Object Properties Test";

    expectedAllIndex0Properties = List.of("Brazil Test", "Canada Test");
    expectedAllIndex1Properties = List.of("Brazilian Real Test", "Canadian Dollar Test");

    actualListObjects = properties.getObjects();

    actualProperty = properties.getTask();
  }

  @Order(1)
  @Test
  void givenTaskProperty_whenBindingToClass_thenPropertyMatchesExpected() {

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
  void givenListObjects_whenCountingItems_thenHasExpectedSize() {

    int expectedTotalObjects = 2;
    assertEquals(expectedTotalObjects, actualListObjects.size());
  }

  @Order(4)
  @Test
  void givenListObjects_whenModifyingGetterResult_thenThrowsException() {

    assertThrows(
         UnsupportedOperationException.class,
         () -> actualListObjects.add(new ListObjects("Test", "Test Currency"))
    );
  }

  @Order(5)
  @Test
  void givenProperties_whenCheckingTypes_thenMatchExpectedClasses() {

    assertAll(() -> assertInstanceOf(String.class, actualProperty));

    actualListObjects
         .forEach(object -> {
           assertInstanceOf(ListObjects.class, object);
         });
  }

  @Order(6)
  @Test
  void givenNewTaskValue_whenUsingPropertySetter_thenValueIsUpdated() {

    String newProperty = "New Binding Object Properties Test Changed";
    properties.setTask(newProperty);
    assertEquals(newProperty, properties.getTask());
  }

  @Order(7)
  @Test
  void givenNewObjectsList_whenUsingListSetter_thenListIsUpdated() {

    var newObjects = List.of(new ListObjects("TestName", "TestCurrency"));
    properties.setObjects(newObjects);
    assertEquals(newObjects, properties.getObjects());
  }

  @Order(8)
  @Test
  void givenNullValue_whenSettingTask_thenAcceptsNull() {

    properties.setTask(null);
    assertNull(properties.getTask());
  }

  @Order(9)
  @Test
  void givenEmptyString_whenSettingTask_thenAcceptsEmpty() {

    properties.setTask("");

    assertEquals("", properties.getTask());
  }

  @Order(10)
  @DirtiesContext
  @Test
  void givenEmptyList_whenSettingObjects_thenAcceptsEmpty() {

    properties.setObjects(List.of());
    assertEquals(0, properties.getObjects()
                              .size()
    );
  }

  @Order(11)
  @Test
  void givenNullList_whenSettingObjects_thenThrowsNullPointerException() {

    properties.setObjects(null);

    assertThrows(
         NullPointerException.class,
         () -> properties.getObjects()
    );
  }

  private void checkPropertiesTotal(Object properties, int expectedTotal) {

    int actualTotal = properties.getClass().getDeclaredFields().length;
    assertEquals(expectedTotal, actualTotal);
  }
}