package ${package}.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

import java.util.Collections;
import java.util.List;

@ConfigurationProperties(prefix = "nation.property")
public class ListPropertiesClass {

  private final String strategy;
  private final List<Country> country;


  @ConstructorBinding
  public ListPropertiesClass(String strategy, List<Country> country) {

    this.strategy = strategy;
    this.country = country;
  }


  public String getStrategy() {

    return strategy;
  }

  public List<Country> getCountry() {

    final List<Country> unmodifiableList = Collections.unmodifiableList(this.country);

    return unmodifiableList;
  }
}