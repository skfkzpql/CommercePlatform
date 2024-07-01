package com.hyunn.commerceplatform.repository;

import com.hyunn.commerceplatform.entity.Terms;
import com.hyunn.commerceplatform.entity.types.TermType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TermsRepository extends JpaRepository<Terms, Long> {

  List<Terms> findByType(TermType termType);
}
