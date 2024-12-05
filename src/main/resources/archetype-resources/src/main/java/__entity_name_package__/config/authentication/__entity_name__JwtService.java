#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.${entity_name_package}.config.authentication;

import java.time.Instant;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

#set($firstChar = $entity_name.substring(0,1).toLowerCase())
#set($restOfName = $entity_name.substring(1))
#set($componentAliasPrefix = "${firstChar}${restOfName}")
@Service(value = "${componentAliasPrefix}JwtService")
public class ${entity_name}JwtService {

  @Autowired
  private JwtEncoder encoder;

  public String generateToken(Authentication authentication) {

    Instant now = Instant.now();

    long expirationPeriod = 36000L;

    String scope =
         authentication
              .getAuthorities()
              .stream()
              .map(GrantedAuthority::getAuthority)
              .collect(Collectors.joining(" "));

    // Claims: Token Properties
    JwtClaimsSet claims_token_properties =
         JwtClaimsSet
              .builder()
              .issuer("spring-security-jwt")
              .issuedAt(now)
              .expiresAt(now.plusSeconds(expirationPeriod))
              .subject(authentication.getName())
              .claim("scope", scope)
              .build();

    return
         encoder
              .encode(JwtEncoderParameters.from(claims_token_properties))
              .getTokenValue();
  }
}