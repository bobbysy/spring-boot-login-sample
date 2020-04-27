package com.example.login.controllers;

import com.example.login.models.CustomPersistentToken;
import com.example.login.models.ERole;
import com.example.login.models.Role;
import com.example.login.models.User;
import com.example.login.payload.request.LoginRequest;
import com.example.login.payload.request.SignupRequest;
import com.example.login.payload.request.TokenVerificationRequest;
import com.example.login.payload.response.JwtResponse;
import com.example.login.payload.response.MessageResponse;
import com.example.login.repository.CustomPersistentTokenRepository;
import com.example.login.repository.RoleRepository;
import com.example.login.repository.UserRepository;
import com.example.login.security.jwt.JwtUtils;
import com.example.login.security.jwt.UserDetailsVerificationUtils;
import com.example.login.security.services.TokenDetailsServiceImpl;
import com.example.login.security.services.UserDetailsImpl;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.ldap.userdetails.LdapUserDetailsImpl;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Handles the signup and login requests.
 *
 * @author sy
 */
@CrossOrigin(origins = "*", maxAge = 3600)
@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthController {
  @Autowired private AuthenticationManager authenticationManager;

  @Autowired private CustomPersistentTokenRepository tokenRepository;

  @Autowired private UserRepository userRepository;

  @Autowired private RoleRepository roleRepository;

  @Autowired private PasswordEncoder encoder;

  @Autowired private TokenDetailsServiceImpl tokenDetailsService;

  @Autowired private JwtUtils jwtUtils;

  @Autowired private UserDetailsVerificationUtils userDetailsVerificationUtils;

  /**
   * Performs the sign in request.
   *
   * @param loginRequest the {@link LoginRequest} payload
   * @return the result
   */
  @PostMapping("/signin")
  public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

    Authentication authentication =
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginRequest.getUsername(), loginRequest.getPassword()));

    SecurityContextHolder.getContext().setAuthentication(authentication);
    String jwt = jwtUtils.generateJwtToken(authentication);

    if (authentication.getPrincipal() instanceof LdapUserDetailsImpl) {
      LdapUserDetailsImpl userDetails = (LdapUserDetailsImpl) authentication.getPrincipal();
      List<String> roles =
          userDetails.getAuthorities().stream()
              .map(GrantedAuthority::getAuthority)
              .collect(Collectors.toList());

      CustomPersistentToken token =
          tokenRepository
              .findByUsername(userDetails.getUsername())
              .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username"));

      return ResponseEntity.ok(
          new JwtResponse(jwt, token.getId(), userDetails.getUsername(), roles));
    } else {
      UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
      List<String> roles =
          userDetails.getAuthorities().stream()
              .map(GrantedAuthority::getAuthority)
              .collect(Collectors.toList());

      return ResponseEntity.ok(
          new JwtResponse(jwt, userDetails.getId(), userDetails.getUsername(), roles));
    }
  }

  /**
   * Performs the sign in request.
   *
   * @param signUpRequest the {@link SignupRequest} payload
   * @return the result
   */
  @PostMapping("/signup")
  public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
    if (userRepository.existsByUsername(signUpRequest.getUsername())) {
      return ResponseEntity.badRequest()
          .body(new MessageResponse("Error: Username is already taken!"));
    }

    // Create new user's account
    User user = new User(signUpRequest.getUsername(), encoder.encode(signUpRequest.getPassword()));

    Set<String> strRoles = signUpRequest.getRole();
    Set<Role> roles = new HashSet<>();

    if (strRoles == null) {
      Role userRole =
          roleRepository
              .findByName(ERole.ROLE_USER)
              .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
      roles.add(userRole);
    } else {
      strRoles.forEach(
          role -> {
            switch (role) {
              case "admin":
                Role adminRole =
                    roleRepository
                        .findByName(ERole.ROLE_ADMIN)
                        .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                roles.add(adminRole);

                break;
              case "mod":
                Role modRole =
                    roleRepository
                        .findByName(ERole.ROLE_MODERATOR)
                        .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                roles.add(modRole);

                break;
              default:
                Role userRole =
                    roleRepository
                        .findByName(ERole.ROLE_USER)
                        .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                roles.add(userRole);
            }
          });
    }

    user.setRoles(roles);
    userRepository.save(user);

    return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
  }

  /**
   * Performs the sign out request.
   *
   * @return the result
   */
  @PostMapping("/signout")
  public ResponseEntity<?> logoutUser() {

    tokenDetailsService.logout(SecurityContextHolder.getContext().getAuthentication());

    return ResponseEntity.ok(new MessageResponse("Logout successfully!"));
  }

  /**
   * Performs the jwt verification.
   *
   * @param tokenVerificationRequest the jwt
   * @return the result
   */
  @PostMapping("/verify")
  public ResponseEntity<?> verifyToken(
      @Valid @RequestBody TokenVerificationRequest tokenVerificationRequest) {
    try {
      String jwt = tokenVerificationRequest.getToken();
      if (jwtUtils.validateJwtToken(jwt)) {
        String username = jwtUtils.getUserNameFromJwtToken(jwt);

        UsernamePasswordAuthenticationToken authenticationToken;
        try {
          // First find the username from dao
          authenticationToken = userDetailsVerificationUtils.verifyFromDao(username);

          UserDetailsImpl userDetails = (UserDetailsImpl) authenticationToken.getPrincipal();

          List<String> roles =
              authenticationToken.getAuthorities().stream()
                  .map(GrantedAuthority::getAuthority)
                  .collect(Collectors.toList());

          return ResponseEntity.ok(new JwtResponse(jwt, userDetails.getId(), username, roles));
        } catch (UsernameNotFoundException ex) {
          // If not find the username from ldap
          authenticationToken = userDetailsVerificationUtils.verifyFromLdap(username, jwt);

          List<String> roles =
              authenticationToken.getAuthorities().stream()
                  .map(GrantedAuthority::getAuthority)
                  .collect(Collectors.toList());

          CustomPersistentToken token =
              tokenRepository
                  .findByUsername(username)
                  .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username"));

          return ResponseEntity.ok(new JwtResponse(jwt, token.getId(), username, roles));
        }
      }
    } catch (Exception e) {
      log.error(e.getMessage());
      return ResponseEntity.badRequest().body(new MessageResponse("Error: Invalid token!"));
    }
    return ResponseEntity.badRequest().body(new MessageResponse("Error: Invalid token!"));
  }
}
