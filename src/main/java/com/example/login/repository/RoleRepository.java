package com.example.login.repository;

import com.example.login.models.ERole;
import com.example.login.models.Role;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository class for <code>Role</code> objects. All method names are compliant with Spring Data
 * naming conventions so this interface can easily be extended for Spring Data. See:
 * https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#repositories.query-methods.query-creation
 *
 * @author sy
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

  /**
   * Retrieve {@link Role} from the data store by role enumeration.
   *
   * @param name Value to search
   * @return a Object matching the {@link Role} (or empty instance)
   */
  Optional<Role> findByName(ERole name);
}
