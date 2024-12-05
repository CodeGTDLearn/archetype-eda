#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.${entity_name};

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

#set($firstChar = $entity_name.substring(0,1).toLowerCase())
#set($restOfName = $entity_name.substring(1))
#set($decapitalizedProjectName = "${firstChar}${restOfName}")
#set($componentAliasPrefix = $decapitalizedProjectName)
#set($injectionPoint = $decapitalizedProjectName)
#set($methodParameter = $decapitalizedProjectName)
@Service(value = "${componentAliasPrefix}Service")
public class ${entity_name}Service {

  @Autowired
  private ${entity_name}Repository ${injectionPoint}Repository;

  public ${entity_name} save${entity_name}(${entity_name} ${methodParameter}) {

    return ${injectionPoint}Repository.save(${methodParameter});
  }

  public List<${entity_name}> getAll${entity_name}s() {

    return ${injectionPoint}Repository.findAll();
  }
}