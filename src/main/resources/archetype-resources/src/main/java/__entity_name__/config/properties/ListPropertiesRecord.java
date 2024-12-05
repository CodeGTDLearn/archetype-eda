package dev.danvega.props.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "nation.property")
public record ListPropertiesRecord(String strategy, List<Country> country) {


}