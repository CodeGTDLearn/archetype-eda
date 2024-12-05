package ${package}.${entity_name_package}.config.properties.map_properties;

import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.MethodOrderer.*;

@SpringBootTest
@TestMethodOrder(OrderAnnotation.class)
class MapPropertiesTest {

  @Autowired
  MapProperties properties;

  List<String> expectedKeys;
  List<MapItem> expectedValues;

  Map<String, MapItem> actualMap;

  @BeforeEach
  void setUp() {

    checkPropertiesTotal(properties.getMap(), 2);

    expectedKeys = List.of("brazil_Test", "spain_Test");
    expectedValues =
         List.of(
              new MapItem("portuguese Test"),
              new MapItem("spanish Test")
         );

    actualMap = properties.getMap();
  }

  @Order(1)
  @Test
  void givenMapProperties_whenBindingData_thenVerifyKeyValuePairs() {

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
  void givenMapProperties_whenBindingData_thenVerifyTotalSize() {

    assertEquals(expectedKeys.size(), actualMap.size());
  }

  @Order(3)
  @Test
  void givenMapProperties_whenInitialized_thenMapShouldNotBeNull() {

    assertNotNull(actualMap);
  }

  @Order(4)
  @Test
  void givenMapProperties_whenBindingData_thenVerifyValueTypes() {

    assertTrue(actualMap.values()
                        .stream()
                        .allMatch(item -> item instanceof MapItem));
  }

  @Order(5)
  @Test
  void givenMapProperties_whenBindingData_thenVerifyKeyOrder() {

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
  void givenMapProperties_whenUsingGetter_thenMapShouldBeUnmodifiable() {

    assertThrows(
         UnsupportedOperationException.class,
         () -> actualMap.put("brazil", new MapItem("newLanguage"))
    );
  }

  @Order(7)
  @Test
  void givenMapProperties_whenUsingSetter_thenMapShouldBeModifiable() {

    var newMap = new HashMap<String, MapItem>();
    newMap.put("uruguai", new MapItem("espanhol-uruguaio"));
    properties.setMap(newMap);

    var uruguaiMap = properties.getMap()
                               .get("uruguai");
    assertEquals("espanhol-uruguaio", uruguaiMap.name());
  }

  private void checkPropertiesTotal(Map<?, ?> properties, int expectedTotal) {

    int actualTotal = properties.size();
    assertEquals(expectedTotal, actualTotal);
  }
}