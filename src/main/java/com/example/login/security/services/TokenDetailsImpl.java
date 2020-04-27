package com.example.login.security.services;

import com.example.login.models.CustomPersistentToken;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Implementation of {@link UserDetails} that provides token information.
 *
 * @author sy
 */
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class TokenDetailsImpl implements UserDetails {
  private static final long serialVersionUID = 2L;

  private Long id;

  private String username;

  @JsonIgnore private String password;

  private Collection<? extends GrantedAuthority> authorities;

  /**
   * Creates the {@link UserDetails} that converts Set<Role> into List<GrantedAuthority> required
   * for Spring Security.
   *
   * @param token Object to create.
   * @return the newly created {@link UserDetails}
   */
  public static TokenDetailsImpl build(CustomPersistentToken token) {
    List<GrantedAuthority> authorities =
        token.getRoles().stream()
            .map(role -> new SimpleGrantedAuthority(role.getName().name()))
            .collect(Collectors.toList());

    // We use the series as password
    return new TokenDetailsImpl(token.getId(), token.getUsername(), token.getSeries(), authorities);
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return authorities;
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }
}
