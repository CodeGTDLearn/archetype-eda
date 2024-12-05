package ${package}.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Component;

@Component
@ConfigurationProperties(prefix = "my.key")
public class KeyProperties {

  private String public_key;
  private String private_key;

  public String getPublic_key() {

    return public_key;
  }

  public String getPrivate_key() {

    return private_key;
  }

  public void setPublic_key(String public_key) {

    this.public_key = public_key;
  }

  public void setPrivate_key(String private_key) {

    this.private_key = private_key;
  }
}