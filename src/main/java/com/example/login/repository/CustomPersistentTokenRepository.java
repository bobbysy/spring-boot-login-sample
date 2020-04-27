package com.example.login.repository;

import com.example.login.models.CustomPersistentToken;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository class for <code>CustomPersistentTokenRepository</code> objects. All method names are
 * compliant with Spring Data naming conventions so this interface can easily be extended for Spring
 * Data. See:
 * https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#repositories.query-methods.query-creation
 *
 * @author sy
 */
@Repository
public interface CustomPersistentTokenRepository
    extends JpaRepository<CustomPersistentToken, Long> {

  CustomPersistentToken findBySeries(String series);

  Long deleteBySeries(String series);

  Optional<CustomPersistentToken> findByUsername(String username);

  Long deleteByUsername(String username);
}
