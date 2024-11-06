#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.${project_name}Security.${project_name}AuthenticationController;

import  ${package}.${project_name}Security.${project_name}AuthenticationService.${project_name}AuthenticationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

#set($firstChar = $project_name.substring(0,1).toLowerCase())
#set($restOfName = $project_name.substring(1))
#set($injectionPoint = "${firstChar}${restOfName}")
@RestController
public class ${project_name}AuthenticationController {

  @Autowired
  private ${project_name}AuthenticationService ${injectionPoint}AuthenticationService;

  @PostMapping("authenticate")
  public String authenticate(Authentication authentication) {

    return ${injectionPoint}AuthenticationService.authenticate(authentication);
  }
}