package com.hyunn.commerceplatform.repository;

import com.hyunn.commerceplatform.entity.Users;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsersRepository extends JpaRepository<Users, Long> {

  Optional<Users> findByUsername(String username);

  Optional<Users> findByEmail(String email);

  boolean existsByUsername(String username);

  boolean existsByEmail(String email);
}
