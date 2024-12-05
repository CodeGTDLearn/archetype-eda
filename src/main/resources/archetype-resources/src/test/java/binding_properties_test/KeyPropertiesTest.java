package dev.danvega.props.list;


import dev.danvega.props.properties.KeyProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class KeyPropertiesTest {

  @Autowired
  private KeyProperties keyProperties;

  String expectedPublicKey;
  String expectedPrivateKey;

  String actualPublicKey;
  String actualPrivateKey;

  @BeforeEach
  void setUp() {

    expectedPublicKey = "classpath:app.key";
    expectedPrivateKey = "classpath:app.pub";

    actualPublicKey = keyProperties.getPublic_key();
    actualPrivateKey = keyProperties.getPrivate_key();
  }


  @Test
  void check_keys() {

    assertEquals(expectedPrivateKey, actualPrivateKey);
    assertEquals(expectedPublicKey, actualPublicKey);
  }

  @Test
  void shouldNotGet_keys_withNullValue() {

    assertNotNull(keyProperties.getPublic_key());
    assertNotNull(keyProperties.getPrivate_key());
  }

  @Test
  void shouldNotGet_keys_withEmptyValue() {

    assertFalse(keyProperties.getPublic_key()
                             .isEmpty());
    assertFalse(keyProperties.getPrivate_key()
                             .isEmpty());
  }
}