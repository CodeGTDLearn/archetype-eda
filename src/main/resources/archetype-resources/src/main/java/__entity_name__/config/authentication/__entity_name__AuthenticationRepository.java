#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.${entity_name}.config.authentication;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

import ${package}.${entity_name}.${entity_name};

public interface ${entity_name}AuthenticationRepository extends CrudRepository<${entity_name}, String> {
  Optional<${entity_name}> findByUsername(String username);
}