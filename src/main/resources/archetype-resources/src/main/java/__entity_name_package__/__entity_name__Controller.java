#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.${entity_name_package};

#if(${enable_denormalization} == "true")
import ${package}.${entity_name_package}.model.${entity_name};
#end
##
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

#set($firstChar = $entity_name.substring(0,1).toLowerCase())
#set($restOfName = $entity_name.substring(1))
#set($decapitalizedProjectName = "${firstChar}${restOfName}")
#set($componentEndpoint = $entity_name.toLowerCase())
#set($injectionPoint = $decapitalizedProjectName)
#set($methodParam = $decapitalizedProjectName)
@RestController
@RequestMapping("/api/${componentEndpoint}s")
public class ${entity_name}Controller {

  @Autowired
  private ${entity_name}Service ${injectionPoint}Service;

  @PostMapping
  public ${entity_name} create${entity_name}(@RequestBody ${entity_name} ${methodParam}) {

    return ${injectionPoint}Service.save${entity_name}(${methodParam});
  }

  @GetMapping
  public List<${entity_name}> getAll${entity_name}s() {

    return ${injectionPoint}Service.getAll${entity_name}s();
  }
}