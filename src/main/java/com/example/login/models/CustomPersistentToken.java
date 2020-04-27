package com.example.login.models;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * CustomPersistentToken model.
 *
 * @author sy
 */
@Entity
@Table(
    name = "persistent_tokens",
    uniqueConstraints = {@UniqueConstraint(columnNames = "username")})
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CustomPersistentToken {

  @Id
  @Column(name = "persistent_tokens_id")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String username;

  private String token;

  private String series;

  @Temporal(TemporalType.TIMESTAMP)
  private Date last_used;

  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(
      name = "token_roles",
      joinColumns = @JoinColumn(name = "persistent_tokens_id"),
      inverseJoinColumns = @JoinColumn(name = "role_id"))
  @Builder.Default
  private Set<Role> roles = new HashSet<>();

  public CustomPersistentToken(String username, String token, String series, Date last_used, Set<Role> roles) {
    this.username = username;
    this.token = token;
    this.series = series;
    this.last_used = last_used;
    this.roles = roles;
  }
}
