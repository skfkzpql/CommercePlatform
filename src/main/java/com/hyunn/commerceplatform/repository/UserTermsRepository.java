package com.hyunn.commerceplatform.repository;

import com.hyunn.commerceplatform.entity.UserTerms;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserTermsRepository extends JpaRepository<UserTerms, Long> {

}
