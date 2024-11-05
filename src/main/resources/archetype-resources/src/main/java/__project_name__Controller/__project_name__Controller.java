#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.${project_name}Controller;

import ${package}.${project_name}.${project_name};
import ${package}.${project_name}Service.${project_name}Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

#set($firstChar = $project_name.substring(0,1).toLowerCase())
#set($restOfName = $project_name.substring(1))
#set($decapitalizedProjectName = "${firstChar}${restOfName}")
#set($componentEndpoint = $project_name.toLowerCase())
#set($injectionPoint = $decapitalizedProjectName)
#set($methodParam = $decapitalizedProjectName)
@RestController
@RequestMapping("/api/${componentEndpoint}s")
public class ${project_name}Controller {

  @Autowired
  private ${project_name}Service ${injectionPoint}Service;

  @PostMapping
  public ${project_name} create${project_name}(@RequestBody ${project_name} ${methodParam}) {

    return ${injectionPoint}Service.save${project_name}(${methodParam});
  }

  @GetMapping
  public List<${project_name}> getAll${project_name}s() {

    return ${injectionPoint}Service.getAll${project_name}s();
  }
}