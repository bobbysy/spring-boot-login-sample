package com.example.login.security.jwt;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Implementation of {@link OncePerRequestFilter} to parse and validate JWT, loading {@link
 * UserDetails}, checking Authorization
 *
 * @author sy
 */
@Slf4j
public class AuthTokenFilter extends OncePerRequestFilter {
  @Autowired private JwtUtils jwtUtils;

  @Autowired private UserDetailsVerificationUtils userDetailsVerificationUtils;

  /**
   * Filter the request to obtain the {@link UserDetails} and set in the {@link SecurityContext}.
   *
   * <p>To get the {@link UserDetails}, use the {@link SecurityContext} like so:
   *
   * <pre> {@code UserDetails userDetails =
   * 	(UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
   * 	}
   * // userDetails.getUsername();
   * // userDetails.getPassword();
   * // userDetails.getAuthorities();</pre>
   *
   * @param request the {@link HttpServletRequest} contains the client's request
   * @param response the {@link HttpServletResponse} contains the filter's response
   * @param filterChain for invoking the next filter or the resource
   * @throws ServletException if an exception occurs that interferes with the filter's normal
   *     operation
   * @throws IOException if an I/O related error has occurred during the processing
   */
  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    try {
      String jwt = parseJwt(request);
      // If the request has JWT, validate it and parse the username
      if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
        String username = jwtUtils.getUserNameFromJwtToken(jwt);

        try {
          // First find the username from dao
          UsernamePasswordAuthenticationToken authentication =
              userDetailsVerificationUtils.verifyFromDao(username);
          authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
          // set the current UserDetails in the {@link SecurityContext}
          SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (UsernameNotFoundException ex) {

          // If not find the username from ldap
          UsernamePasswordAuthenticationToken authentication =
              userDetailsVerificationUtils.verifyFromLdap(username, jwt);
          authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
          SecurityContextHolder.getContext().setAuthentication(authentication);
        }
      }
    } catch (Exception e) {
      log.error("Cannot set user authentication: {}", e.getMessage());
    }
    filterChain.doFilter(request, response);
  }

  /**
   * Parse the JWT and obtain the Authorization header by removing Bearer prefix.
   *
   * @param request the {@link HttpServletRequest} contains the client's request
   * @return the stripped header or null if Authorization header does not exists
   */
  private String parseJwt(HttpServletRequest request) {
    String headerAuth = request.getHeader("Authorization");

    if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
      return headerAuth.substring(7);
    }

    return null;
  }
}
