#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.${entity_name_package}.config.authentication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

#set($firstChar = $entity_name.substring(0,1).toLowerCase())
#set($restOfName = $entity_name.substring(1))
#set($componentAliasPrefix = "${firstChar}${restOfName}")
@Service(value = "${componentAliasPrefix}AuthenticationService")
public class ${entity_name}AuthenticationService {

  @Autowired
  private ${entity_name}JwtService jwtService;

  public String authenticate(Authentication authentication) {

    return jwtService.generateToken(authentication);
  }
}