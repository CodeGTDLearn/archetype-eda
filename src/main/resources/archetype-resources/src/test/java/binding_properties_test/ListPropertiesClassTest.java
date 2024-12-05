package dev.danvega.props.list;

import dev.danvega.props.properties.ListPropertiesClass;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class ListPropertiesClassTest {

  @Autowired
  ListPropertiesClass properties;

  private static final Logger log = LoggerFactory.getLogger(ListPropertiesClassTest.class);

  String expected_property = "Country";

  List<String> expected_property1_from_list_item;
  List<String> expected_property2_from_list_item;

  @BeforeEach
  void setUp() {

    expected_property = "Country";
    expected_property1_from_list_item = List.of("Brazil", "Canada");
    expected_property2_from_list_item = List.of("Brazilian Real", "Canadian Dollar");
  }

  @Test
  void checkPropertyItemDataBinding() {

    var actual_property = properties.getStrategy();

    assertEquals(expected_property, actual_property, "property must match");

    log.info("Property: {}", actual_property);
  }

  @Test
  void checkPropertyListDataBinding() {

    var actual_list_items = properties.getCountry();

    assertEquals(
         expected_property1_from_list_item.size(),
         actual_list_items.size(),
         "Error: Number of properties"
    );

    IntStream.range(0, actual_list_items.size())
             .forEach(index -> {

               var property_1 = expected_property1_from_list_item.get(index);
               var property_2 = expected_property2_from_list_item.get(index);

               var actual_item = actual_list_items.get(index);

               assertAll("List Property Item " + index,
                         () -> assertEquals(property_1, actual_item.name(), "name must match"),
                         () -> assertEquals(property_2, actual_item.currency(),
                                            "currency must match"
                         )
               );

               log.info("No.{} - Property {} - {}", ++ index, actual_item.name(),
                        actual_item.currency()
               );
             });
  }
}