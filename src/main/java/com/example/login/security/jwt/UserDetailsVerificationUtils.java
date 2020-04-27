package com.example.login.security.jwt;

import com.example.login.security.services.TokenDetailsServiceImpl;
import com.example.login.security.services.UserDetailsServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.rememberme.CookieTheftException;
import org.springframework.stereotype.Component;

/**
 * Provides the UserDetails Verification utility functions.
 *
 * @author sy
 */
@Component
@Slf4j
public class UserDetailsVerificationUtils {

  @Autowired private UserDetailsServiceImpl userDetailsService;

  @Autowired private TokenDetailsServiceImpl tokenDetailsService;

  /**
   * Find the username from the Dao to obtain the {@link UserDetails}.
   *
   * @param username Value to search
   * @return the {@link UsernamePasswordAuthenticationToken}
   * @throws UsernameNotFoundException
   */
  public UsernamePasswordAuthenticationToken verifyFromDao(String username)
      throws UsernameNotFoundException {
    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

    return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
  }

  /**
   * Find the username from the Ldap to obtain the {@link UserDetails}.
   *
   * @param username Value to search
   * @return the {@link UsernamePasswordAuthenticationToken}
   * @throws UsernameNotFoundException
   */
  public UsernamePasswordAuthenticationToken verifyFromLdap(String username, String jwt)
      throws UsernameNotFoundException {
    UserDetails tokenUserDetails = tokenDetailsService.loadUserByUsername(username);

    // We have a match for this jwt/series combination
    if (!tokenDetailsService.compareSeries(jwt, tokenUserDetails.getPassword())) {
      // Token doesn't match series value. Delete all logins for this user and throw
      // an exception to warn them.
      tokenDetailsService.deleteToken(username);

      throw new CookieTheftException(
          "Invalid token (Series/token) mismatch. Implies previous cookie theft attack.");
    }

    return new UsernamePasswordAuthenticationToken(
        tokenUserDetails, null, tokenUserDetails.getAuthorities());
  }
}
