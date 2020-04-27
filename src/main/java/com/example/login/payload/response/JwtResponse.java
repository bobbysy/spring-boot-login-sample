package com.example.login.payload.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JwtResponse payload model.
 *
 * @author sy
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class JwtResponse {
  private String token;
  @Builder.Default private String type = "Bearer";
  private Long id;
  private String username;
  private List<String> roles;

  public JwtResponse(String accessToken, Long id, String username, List<String> roles) {
    this.token = accessToken;
    this.id = id;
    this.username = username;
    this.roles = roles;
    this.type = "Bearer";
  }
}
