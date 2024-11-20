#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.${project_name}Service;

import ${package}.${project_name}.${project_name};
import ${package}.${project_name}Repository.${project_name}Repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

#set($firstChar = $project_name.substring(0,1).toLowerCase())
#set($restOfName = $project_name.substring(1))
#set($decapitalizedProjectName = "${firstChar}${restOfName}")
#set($componentAliasPrefix = $decapitalizedProjectName)
#set($injectionPoint = $decapitalizedProjectName)
#set($methodParameter = $decapitalizedProjectName)
@Service(value = "${componentAliasPrefix}Service")
public class ${project_name}Service {

  @Autowired
  private ${project_name}Repository ${injectionPoint}Repository;

  public ${project_name} save${project_name}(${project_name} ${methodParameter}) {

    return ${injectionPoint}Repository.save(${methodParameter});
  }

  public List<${project_name}> getAll${project_name}s() {

    return ${injectionPoint}Repository.findAll();
  }
}