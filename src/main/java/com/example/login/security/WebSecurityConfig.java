package com.example.login.security;

import com.example.login.security.jwt.AuthEntryPointJwt;
import com.example.login.security.jwt.AuthTokenFilter;
import com.example.login.security.services.UserDetailsServiceImpl;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.DefaultDirObjectFactory;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.ldap.authentication.ad.ActiveDirectoryLdapAuthenticationProvider;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Provides the {@link HttpSecurity} configurations to configure cors, csrf, session management,
 * rules for protected resources.
 *
 * @author sy
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(
    securedEnabled = true,
    // jsr250Enabled = true,
    prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

  @Value("${examplelogin.app.ad.domain}")
  private String domainName;

  @Value("${examplelogin.app.ad.url}")
  private String activeDirectoryUrl;

  @Value("${examplelogin.app.jwtSecret}")
  private String jwtSecret;

  @Autowired UserDetailsServiceImpl userDetailsService;

  @Autowired private AuthEntryPointJwt unauthorizedHandler;

  @Autowired private DataSource dataSource;

  @Bean
  public AuthTokenFilter authenticationJwtTokenFilter() {
    return new AuthTokenFilter();
  }

  /**
   * Used to specify the {@link AuthenticationManager} to be used.
   *
   * @param auth the {@link AuthenticationManagerBuilder} to use
   * @throws Exception if an error occurs
   */
  @Override
  public void configure(AuthenticationManagerBuilder auth) throws Exception {

    //    auth.ldapAuthentication()
    //        .userSearchFilter("(&(objectClass=user)(sAMAccountName={0}))")
    //        .groupSearchBase("ou=Organizational")
    //        .groupSearchFilter("(&(objectClass=group)(member={0}))")
    //        .groupRoleAttribute("CN")
    //        .rolePrefix("none")
    //        .contextSource()
    //        .url(activeDirectoryUrl + "DC=ad,DC=test,DC=com")
    //        .managerDn("ldapadmin@ad.test.com")
    //        .managerPassword("P@ssw0rd1");

    auth.authenticationProvider(activeDirectoryLdapAuthenticationProvider());

    // Configure the {@link DaoAuthenticationProvider} to use {@link userDetailsService} as the
    // configuration
    auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
  }

  // TEST 1
  @Bean
  public LdapTemplate ldapTemplate() {
    LdapTemplate ldap = new LdapTemplate(getLdapContext());
    return ldap;
  }

  @Bean
  public LdapContextSource getLdapContext() {
    LdapContextSource lcs = new LdapContextSource();
    lcs.setUrl(activeDirectoryUrl);
    lcs.setBase("DC=ad,DC=test,DC=com");
    lcs.setUserDn("ldapadmin@ad.test.com");
    lcs.setPassword("P@ssw0rd1");
    lcs.setDirObjectFactory(DefaultDirObjectFactory.class);
    lcs.afterPropertiesSet();
    return lcs;
  }

  // Explict datasource in case the autowired not working
  //  @Bean
  //  public DataSource getDataSource() {
  //     DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
  //     return dataSourceBuilder.build();
  //  }

  // TEST END

  /**
   * Create the {@link ActiveDirectoryLdapAuthenticationProvider} with the domain and URL.
   *
   * @return the {@link ActiveDirectoryLdapAuthenticationProvider}
   */
  @Bean
  public AuthenticationProvider activeDirectoryLdapAuthenticationProvider() {
    ActiveDirectoryLdapAuthenticationProvider adProvider =
        new ActiveDirectoryLdapAuthenticationProvider(
            domainName, activeDirectoryUrl, "OU=Users,OU=Organizational,DC=ad,DC=test,DC=com");
    adProvider.setConvertSubErrorCodesToExceptions(true);
    adProvider.setUseAuthenticationRequestCredentials(true);

    return adProvider;
  }

  /**
   * Expose the {@link AuthenticationManager} from {@link #configure(AuthenticationManagerBuilder)}.
   *
   * @return the {@link AuthenticationManager}
   * @throws Exception if an error occurs
   */
  @Bean
  @Override
  public AuthenticationManager authenticationManagerBean() throws Exception {
    return super.authenticationManagerBean();
  }

  /**
   * PasswordEncoder for the DaoAuthenticationProvider, if not specified plain text would be used.
   *
   * @return the {@link BCryptPasswordEncoder}
   */
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  /**
   * Instructs {@link HttpSecurity} the CORS and CSRF configuration, the need to authenticated all
   * users or not, the filter {@link AuthTokenFilter} and when to use it (before {@link
   * UsernamePasswordAuthenticationFilter} as well as the Exception Handler {@link
   * AuthEntryPointJwt} chosen.
   *
   * @param http the {@link HttpSecurity} to modify
   * @throws Exception if an error occurs
   */
  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.cors()
        .and()
        .csrf()
        .disable()
        .exceptionHandling()
        .authenticationEntryPoint(unauthorizedHandler)
        .and()
        .sessionManagement()
        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        .and()
        .authorizeRequests()
        .antMatchers("/api/auth/**")
        .permitAll()
        .and()
        .authorizeRequests()
        .antMatchers("/api/test/**")
        .permitAll()
        .anyRequest()
        .authenticated()
        .and()
        .headers()
        .contentSecurityPolicy("script-src 'self'");
    http.addFilterBefore(
        authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);
  }
}
