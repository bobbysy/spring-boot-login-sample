package com.example.login.payload.request;

import java.util.Set;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * SignupRequest payload model.
 *
 * @author sy
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SignupRequest {

  @NotEmpty
  @Size(min = 3, max = 20)
  private String username;

  private Set<String> role;

  @NotEmpty
  @Size(min = 6, max = 40)
  private String password;
}
