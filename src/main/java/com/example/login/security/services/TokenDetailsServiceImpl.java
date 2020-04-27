package com.example.login.security.services;

import static org.springframework.ldap.query.LdapQueryBuilder.query;

import com.example.login.models.CustomPersistentToken;
import com.example.login.models.ERole;
import com.example.login.models.Role;
import com.example.login.repository.CustomPersistentTokenRepository;
import com.example.login.repository.RoleRepository;
import java.util.Base64;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of {@link UserDetailsService} that provides {@link CustomPersistentToken} object
 * using {@link CustomPersistentTokenRepository}.
 *
 * @author sy
 */
@Service
@Slf4j
public class TokenDetailsServiceImpl implements UserDetailsService {

  @Autowired private CustomPersistentTokenRepository tokenRepository;

  @Autowired private LdapTemplate ldapTemplate;

  @Autowired private RoleRepository roleRepository;

  /**
   * Build the {@link UserDetails} of the {@link CustomPersistentToken} object found in the data
   * store. {@link UserDetails} contains the necessary information (such as: username, password,
   * authorities) to build an Authentication object.
   *
   * @param username Value to search
   * @return the {@link UserDetails} of the {@link CustomPersistentToken} object that Spring
   *     Security can use for authentication and validation
   * @throws UsernameNotFoundException if an error occurs
   */
  @Override
  @Transactional
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    if (existsInLdap(username)) {
      CustomPersistentToken token =
          tokenRepository
              .findByUsername(username)
              .orElseThrow(
                  () -> new UsernameNotFoundException("User Not Found with username: " + username));

      return TokenDetailsImpl.build(token);
    } else {
      throw new UsernameNotFoundException("User Not Found with username: " + username);
    }
  }

  /**
   * Looks up username to verify it exists
   *
   * @param username Value to search
   * @return true/false if exists
   */
  public boolean existsInLdap(String username) {
    LdapQuery query =
        query()
            .base("OU=Users,OU=Organizational")
            .attributes("cn", "sAMAccountName")
            .where("objectclass")
            .is("person")
            .and("sAMAccountName")
            .is(username);

    return !(ldapTemplate
        .search(query, (AttributesMapper<String>) attrs -> (String) attrs.get("cn").get())
        .isEmpty());
  }

  /**
   * Creates a new persistent login token with a new series number, stores the data in the
   * persistent token repository.
   *
   * @param successfulAuthentication the successful {@link Authentication} object.
   * @param jwtToken the generated jwt.
   * @param issuedAt the {@link Date} the jwt was issued.
   */
  public void onLoginSuccess(
      Authentication successfulAuthentication, String jwtToken, Date issuedAt) {
    String username = successfulAuthentication.getName();

    // Generate the roles from the list of authorities
    Set<Role> roles =
        successfulAuthentication.getAuthorities().stream()
            .map(
                grantedAuthority -> {
                  // Ignore "ROLE_" e.g. "ROLE_USER" > "USER"
                  switch (grantedAuthority.getAuthority().substring(5).toLowerCase()) {
                    case "admin":
                      Role role =
                          roleRepository
                              .findByName(ERole.ROLE_ADMIN)
                              .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                      return role;
                    case "user":
                    default:
                      Role userRole =
                          roleRepository
                              .findByName(ERole.ROLE_USER)
                              .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                      return userRole;
                  }
                })
            .collect(Collectors.toSet());

    CustomPersistentToken token =
        new CustomPersistentToken(
            username, jwtToken, generateSeriesData(jwtToken), issuedAt, roles);

    try {
      createNewToken(token);
    } catch (Exception e) {
      log.error("Failed to save persistent token: {}", e.getMessage());
    }
  }

  /**
   * Generates a unique series identifier, used for identifying a user.
   *
   * @param token the jwt.
   * @return the generated series.
   */
  private String generateSeriesData(String token) {
    // Simple encode
    byte[] bytesEncoded = Base64.getEncoder().encode(token.getBytes());

    return new String(bytesEncoded);
  }

  public boolean compareSeries(String token, String series) {
    // Decode data on other side, by processing encoded data
    byte[] valueDecoded = Base64.getDecoder().decode((series));

    return (token.equals(new String(valueDecoded)));
  }

  @Transactional
  void createNewToken(CustomPersistentToken token) {
    CustomPersistentToken current = tokenRepository.findBySeries(token.getSeries());

    if (current != null) {
      throw new DataIntegrityViolationException(
          "Series Id '" + token.getSeries() + "' already exists!");
    }

    tokenRepository.save(token);
  }

  @Transactional
  public void logout(Authentication authentication) {
    if (authentication != null) {
      if (authentication.getPrincipal() instanceof TokenDetailsImpl) {
        TokenDetailsImpl tokenPrincial = (TokenDetailsImpl) authentication.getPrincipal();
        tokenRepository.deleteBySeries(tokenPrincial.getPassword());
      }
    }
  }

  @Transactional
  public void deleteToken(String username) {
    tokenRepository.deleteByUsername(username);
  }
}
