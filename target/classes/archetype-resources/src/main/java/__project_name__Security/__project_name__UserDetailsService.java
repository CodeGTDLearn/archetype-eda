#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.${project_name}Security;

import  ${package}.${project_name}Security.${project_name}AuthenticationRepository.${project_name}AuthenticationRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

#set($firstChar = $project_name.substring(0,1).toLowerCase())
#set($restOfName = $project_name.substring(1))
#set($componentAliasPrefix = "${firstChar}${restOfName}")
#set($injectionPoint = "${firstChar}${restOfName}")
@Service(value = "${componentAliasPrefix}UserDetailsService")
public class ${project_name}UserDetailsService implements UserDetailsService {

  @Autowired
  private ${project_name}AuthenticationRepository ${injectionPoint}AuthenticationRepository;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    return
         ${injectionPoint}AuthenticationRepository
         .findByUsername(username)
         .map(${project_name}UserDetails::new)
         .orElseThrow(
            () -> new UsernameNotFoundException("User Not Found with username: " + username)
          );
  }

}