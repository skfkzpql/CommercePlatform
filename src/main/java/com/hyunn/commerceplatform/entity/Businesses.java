package com.hyunn.commerceplatform.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Businesses {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  private Users user;

  @Column(nullable = false, length = 50, unique = true)
  private String registrationNumber;

  @Column(nullable = false, length = 100)
  private String businessName;

  @Column(nullable = false, length = 255)
  private String address;

  @Column(nullable = false, length = 100)
  private String businessStatus;

  @Column(nullable = false, length = 100)
  private String businessType;

  @Column(nullable = false, length = 100)
  private String representativeName;

  @Column(nullable = false, length = 100)
  private String representativePhone;

  @Column(nullable = false, updatable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Date createdAt;

  @OneToMany(mappedBy = "business")
  private List<Products> products;

  @PrePersist
  protected void onCreate() {
    createdAt = new Date();
  }

}

