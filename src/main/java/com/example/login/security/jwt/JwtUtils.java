package com.example.login.security.jwt;

import com.example.login.security.services.TokenDetailsServiceImpl;
import com.example.login.security.services.UserDetailsImpl;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.ldap.userdetails.LdapUserDetailsImpl;
import org.springframework.stereotype.Component;

/**
 * Provides the JWT utility functions.
 *
 * @author sy
 */
@Component
@Slf4j
public class JwtUtils {

  @Value("${examplelogin.app.jwtSecret}")
  private String jwtSecret;

  @Value("${examplelogin.app.jwtExpirationMs}")
  private int jwtExpirationMs;

  @Autowired private TokenDetailsServiceImpl tokenDetailsService;

  /**
   * Generate a JWT from username, date, expiration, secret.
   *
   * @param authentication Value to create the JWT from
   * @return the newly created {@link Jwts}
   */
  public String generateJwtToken(Authentication authentication) {

    Date now = new Date();
    Date validity = new Date(now.getTime() + jwtExpirationMs);

    if (authentication.getPrincipal() instanceof LdapUserDetailsImpl) {
      LdapUserDetailsImpl userPrincipal = (LdapUserDetailsImpl) authentication.getPrincipal();

      String jwtToken =
          Jwts.builder()
              .setSubject((userPrincipal.getUsername()))
              .setIssuedAt(now)
              .setExpiration(validity)
              .signWith(SignatureAlgorithm.HS512, jwtSecret)
              .compact();

      tokenDetailsService.onLoginSuccess(authentication, jwtToken, now);

      return jwtToken;
    } else {
      UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();

      return Jwts.builder()
          .setSubject((userPrincipal.getUsername()))
          .setIssuedAt(now)
          .setExpiration(validity)
          .signWith(SignatureAlgorithm.HS512, jwtSecret)
          .compact();
    }
  }

  /**
   * Retrieve the username from the given JWT.
   *
   * @param token Value to retrieve from
   * @return the username
   */
  public String getUserNameFromJwtToken(String token) {
    return Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody().getSubject();
  }

  /**
   * Validate the JWT.
   *
   * @param authToken Value to be validated.
   * @return true/false of the validation outcome
   */
  public boolean validateJwtToken(String authToken) {
    try {
      Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(authToken);
      return true;
    } catch (SignatureException e) {
      log.error("Invalid JWT signature: {}", e.getMessage());
    } catch (MalformedJwtException e) {
      log.error("Invalid JWT token: {}", e.getMessage());
    } catch (ExpiredJwtException e) {
      log.error("JWT token is expired: {}", e.getMessage());
    } catch (UnsupportedJwtException e) {
      log.error("JWT token is unsupported: {}", e.getMessage());
    } catch (IllegalArgumentException e) {
      log.error("JWT claims string is empty: {}", e.getMessage());
    }

    return false;
  }
}
