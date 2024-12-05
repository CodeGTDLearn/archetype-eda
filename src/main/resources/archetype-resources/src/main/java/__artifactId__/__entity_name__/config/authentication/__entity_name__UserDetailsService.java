#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${project_package}.${entity_name}.config.authentication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

#set($firstChar = $entity_name.substring(0,1).toLowerCase())
#set($restOfName = $entity_name.substring(1))
#set($componentAliasPrefix = "${firstChar}${restOfName}")
#set($injectionPoint = "${firstChar}${restOfName}")
@Service(value = "${componentAliasPrefix}UserDetailsService")
public class ${entity_name}UserDetailsService implements UserDetailsService {

  @Autowired
  private ${entity_name}AuthenticationRepository ${injectionPoint}AuthenticationRepository;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    return
         ${injectionPoint}AuthenticationRepository
         .findByUsername(username)
         .map(${entity_name}UserDetails::new)
         .orElseThrow(
            () -> new UsernameNotFoundException("User Not Found with username: " + username)
          );
  }

}