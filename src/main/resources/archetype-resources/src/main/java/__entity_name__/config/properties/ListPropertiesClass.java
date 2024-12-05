package dev.danvega.props.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "nation.property")
public class ListPropertiesClass {

  private String strategy;
  private List<Country> country;

  public void setStrategy(String strategy) {

    this.strategy = strategy;
  }

  public void setCountry(List<Country> country) {

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