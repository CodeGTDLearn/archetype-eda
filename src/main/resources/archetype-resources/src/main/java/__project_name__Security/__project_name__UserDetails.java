#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.${project_name}Security;

import ${package}.${project_name}.${project_name};

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

#if(${enable_lombok} == "true")
import lombok.AllArgsConstructor;

@AllArgsConstructor
#end
#set($firstChar = $project_name.substring(0,1).toLowerCase())
#set($restOfName = $project_name.substring(1))
#set($componentAliasPrefix = "${firstChar}${restOfName}")
#set($userDetails = $project_name.toLowerCase())
@Service(value = "${componentAliasPrefix}UserDetails")
public class ${project_name}UserDetails implements UserDetails {

  private final ${project_name} ${userDetails};

#if(${enable_lombok} == "false")
  public ${project_name}UserDetails (${project_name} ${userDetails}) {

    this.${userDetails} = ${userDetails};
  }
#end

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {

    return List.of(() -> "read");
  }

  @Override
  public String getPassword() {

    return ${userDetails}.getPassword();
  }

  @Override
  public String getUsername() {

    return ${userDetails}.getUsername();
  }

  @Override
  public boolean isAccountNonExpired() {

    return UserDetails.super.isAccountNonExpired();
  }

  @Override
  public boolean isAccountNonLocked() {

    return UserDetails.super.isAccountNonLocked();
  }

  @Override
  public boolean isCredentialsNonExpired() {

    return UserDetails.super.isCredentialsNonExpired();
  }

  @Override
  public boolean isEnabled() {

    return UserDetails.super.isEnabled();
  }
}