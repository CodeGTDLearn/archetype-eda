#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.${entity_name_package}.config.authentication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

#set($firstChar = $entity_name.substring(0,1).toLowerCase())
#set($restOfName = $entity_name.substring(1))
#set($injectionPoint = "${firstChar}${restOfName}")
@RestController
public class ${entity_name}AuthenticationController {

  @Autowired
  private ${entity_name}AuthenticationService ${injectionPoint}AuthenticationService;

  @PostMapping("authenticate")
  public String authenticate(Authentication authentication) {

    return ${injectionPoint}AuthenticationService.authenticate(authentication);
  }
}