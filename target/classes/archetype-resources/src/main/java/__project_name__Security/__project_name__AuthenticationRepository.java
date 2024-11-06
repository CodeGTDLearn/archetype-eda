#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.${project_name}Security.${project_name}AuthenticationRepository;

import org.springframework.data.repository.CrudRepository;
import java.util.Optional;

import ${package}.${project_name}.${project_name};

public interface ${project_name}AuthenticationRepository extends CrudRepository<${project_name}, String> {
  Optional<${project_name}> findByUsername(String username);
}