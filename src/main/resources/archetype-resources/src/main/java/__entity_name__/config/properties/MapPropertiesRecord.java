package dev.danvega.props.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties("languages")
public record MapPropertiesRecord(Map<String, Language> map_country_language) {
}