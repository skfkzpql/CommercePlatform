package com.hyunn.commerceplatform.dto.users;

import com.hyunn.commerceplatform.entity.types.UserType;
import java.util.Date;
import lombok.Data;

@Data
public class UsersDetailResponseDto {

  private Long id;
  private String username;
  private Date dateOfBirth;
  private String email;
  private boolean emailVerified;
  private UserType userType;
  private Date createdAt;
  private Date updatedAt;
}
