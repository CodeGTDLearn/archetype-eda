#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.${project_name}Security.${project_name}AuthenticationService;

import ${package}.${project_name}Security.${project_name}JwtService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

#set($firstChar = $project_name.substring(0,1).toLowerCase())
#set($restOfName = $project_name.substring(1))
#set($componentAliasPrefix = "${firstChar}${restOfName}")
@Service(value = "${componentAliasPrefix}AuthenticationService")
public class ${project_name}AuthenticationService {

  @Autowired
  private ${project_name}JwtService jwtService;

  public String authenticate(Authentication authentication) {

    return jwtService.generateToken(authentication);
  }
}