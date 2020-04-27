package com.example.login.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

import com.example.login.BaseIntegrationTest;
import com.example.login.models.User;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;

public class UserRepositoryIntegrationTests extends BaseIntegrationTest {

  @MockBean UserRepository userRepository;

  @Before
  public void setUp() {
    User user = new User("johndoe", "somerandompassword");
    Mockito.when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));

    Mockito.when(userRepository.existsByUsername(user.getUsername())).thenReturn(true);
  }

  @Test
  public void findByName_whenExists() {
    Optional<User> user = userRepository.findByUsername("johndoe");

    assertThat(user)
        .hasValueSatisfying(
            it -> {
              assertThat(it.getUsername()).isEqualTo("johndoe");
              assertThat(it.getPassword()).isEqualTo("somerandompassword");
            });
  }

  @Test
  public void findByName_whenNotExists() {
    Optional<User> user = userRepository.findByUsername("peter");
    assertThat(user).isEmpty();
  }

  @Test
  public void existsByUsername() {
    boolean userExists = userRepository.existsByUsername("johndoe");

    assertTrue(userExists);
  }
}
