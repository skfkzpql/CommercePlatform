package com.hyunn.commerceplatform.entity;

import com.hyunn.commerceplatform.entity.types.OrderStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItems {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "order_id", nullable = false)
  private Orders order;

  @ManyToOne
  @JoinColumn(name = "product_id", nullable = false)
  private Products product;

  @Column(nullable = false)
  private int quantity;

  @Column(nullable = false)
  private int price;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 50)
  private OrderStatus status;

  @Column(nullable = false, updatable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Date createdAt;

  @Column(nullable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Date updatedAt;

  @ManyToOne
  @JoinColumn(name = "discount_code_id")
  private DiscountCodes discountCode;

  @OneToOne(mappedBy = "orderItem", cascade = CascadeType.ALL, orphanRemoval = true)
  private Reviews review;

  @PrePersist
  protected void onCreate() {
    createdAt = new Date();
    updatedAt = new Date();
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = new Date();
  }
}
