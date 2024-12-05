package ${package}.${entity_name}.config.properties;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MapPropertiesRecordTest {

  @Autowired
  MapPropertiesRecord properties;

  List<String> expectedKeys;
  List<MapItem> expectedValues;

  Map<String, MapItem> actualMap;

  @BeforeEach
  void setUp() {

    expectedKeys = List.of("brazil_Test", "spain_Test");
    expectedValues =
         List.of(
              new MapItem("portuguese Test"),
              new MapItem("spanish Test")
         );

    actualMap = properties.map();
  }

  @Order(1)
  @Test
  void givenMapProperties_whenBindingData_thenAllKeyValuePairsMatch() {

    IntStream
         .range(0, actualMap.size())
         .forEach(index -> {

           var expectedKey = expectedKeys.get(index);
           var expectedValue = expectedValues.get(index);

           var actualKey = actualMap.keySet()
                                    .toArray()[index];
           var actualValue = actualMap.get(actualKey);

           assertAll("Map Property - KV-Pair No. " + ++ index,
                     () -> assertEquals(expectedKey, actualKey),
                     () -> assertEquals(expectedValue, actualValue)
           );
         });
  }

  @Order(2)
  @Test
  void givenMapProperties_whenBindingData_thenTotalPropertiesMatch() {

    assertEquals(expectedKeys.size(), actualMap.size());
  }

  @Order(3)
  @Test
  void givenMapProperties_whenBindingData_thenMapIsNotNull() {

    assertNotNull(actualMap);
  }

  @Order(4)
  @Test
  void givenMapProperties_whenBindingData_thenAllValuesAreOfTypeMapItem() {

    assertTrue(actualMap.values()
                        .stream()
                        .allMatch(item -> item instanceof MapItem));
  }

  @Order(5)
  @Test
  void givenMapProperties_whenBindingData_thenKeyOrderIsPreserved() {

    var keys = actualMap.keySet()
                        .toArray();

    IntStream
         .range(0, keys.length)
         .forEach(index -> {
           var actualKey = keys[index];
           assertEquals(expectedKeys.get(index), actualKey);
         });
  }

  @Order(6)
  @Test
  void givenMapProperties_whenModifyingMap_thenThrowUnsupportedOperationException() {

    assertThrows(
         UnsupportedOperationException.class,
         () -> actualMap.put("brazil", new MapItem("newLanguage"))
    );
  }
}