package com.hyunn.commerceplatform.repository;

import com.hyunn.commerceplatform.entity.Businesses;
import com.hyunn.commerceplatform.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BusinessRepository extends JpaRepository<Businesses, Long> {

  boolean existsByUser(Users user);

  boolean existsByRegistrationNumber(String bNo);
}
