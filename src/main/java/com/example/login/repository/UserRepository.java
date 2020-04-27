package com.example.login.repository;

import com.example.login.models.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository class for <code>User</code> objects. All method names are compliant with Spring Data
 * naming conventions so this interface can easily be extended for Spring Data. See:
 * https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#repositories.query-methods.query-creation
 *
 * @author sy
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
  /**
   * Retrieve {@link User} from the data store by username.
   *
   * @param username Value to search
   * @return a Object matching the {@link User} (or empty instance)
   */
  Optional<User> findByUsername(String username);

  /**
   * Verify {@link User} exists from the data store by username.
   *
   * @param username Value to search
   * @return true/false if {@link User} exists in the data store
   */
  Boolean existsByUsername(String username);
}
