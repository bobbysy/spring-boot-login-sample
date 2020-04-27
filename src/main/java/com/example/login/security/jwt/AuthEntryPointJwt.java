package com.example.login.security.jwt;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

/**
 * Implementation of {@link AuthenticationEntryPoint} to handle unauthenticated User requests.
 *
 * @author sy
 */
@Component
@Slf4j
public class AuthEntryPointJwt implements AuthenticationEntryPoint {

  /**
   * Triggers when unauthenticated User requests a secured HTTP resource and throws an
   * AuthenticationException. {@link HttpServletResponse#SC_UNAUTHORIZED} is the 401 Status code. It
   * indicates that the request requires HTTP authentication.
   *
   * @param request the {@link HttpServletRequest} contains the client's request
   * @param response the {@link HttpServletResponse} contains the filter's response
   * @param authException the cause of the invocation
   * @throws IOException if an I/O related error has occurred during the processing
   * @throws ServletException if an exception occurs that interferes with the filter's normal *
   *     operation
   */
  @Override
  public void commence(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException authException)
      throws IOException, ServletException {
    log.error("Unauthorized error: {}", authException.getMessage());
    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Error: Unauthorized");
  }
}
