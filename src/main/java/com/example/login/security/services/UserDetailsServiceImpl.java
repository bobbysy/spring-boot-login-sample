package com.example.login.security.services;

import com.example.login.models.User;
import com.example.login.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of {@link UserDetailsService} that provides full custom {@link User} object using
 * {@link UserRepository}.
 *
 * @author sy
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

  @Autowired private UserRepository userRepository;

  /**
   * Build the {@link UserDetails} of the {@link User} object found in the data store. {@link
   * UserDetails} contains the necessary information (such as: username, password, authorities) to
   * build an Authentication object.
   *
   * @param username Value to search
   * @return the {@link UserDetails} of the {@link User} object that Spring Security can use for
   *     authentication and validation
   * @throws UsernameNotFoundException if an error occurs
   */
  @Override
  @Transactional
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    User user =
        userRepository
            .findByUsername(username)
            .orElseThrow(
                () -> new UsernameNotFoundException("User Not Found with username: " + username));

    return UserDetailsImpl.build(user);
  }
}
