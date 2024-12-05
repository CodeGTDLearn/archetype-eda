package dev.danvega.props.properties;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class MapPropertiesClassTest {

  @Autowired
  MapPropertiesClass properties;

  private static final Logger log = LoggerFactory.getLogger(MapPropertiesClassTest.class);

  List<String> expected_key;
  List<Language> expected_values;

  @BeforeEach
  void setUp() {

    expected_key = List.of("brazil", "spain");
    expected_values = List.of(new Language("portuguese"), new Language("spanish"));
  }

  @Test
  void shouldGet_mapProperty() {


    Map<String, Language> actual_map_items = properties.getMapCountryLanguage();


    IntStream.range(0, actual_map_items.size())
             .forEach(index -> {

               var key = expected_key.get(index);
               var value = expected_values.get(index);

               var actual_key = actual_map_items.keySet()
                                                .toArray()[index];
               var actual_value = actual_map_items.get(actual_key);

               assertAll("Map Property - Position-Key: " + ++ index,
                         () -> assertEquals(key, actual_key, "key must match"),
                         () -> assertEquals(value, actual_value, "value must match")
               );

               log.info("No.{} - Property Key: {} - Value: {}", index, actual_key, actual_value);
             });
  }
}