## spring-boot-login-sample

Sample application to demonstrate user authentication with Spring security, 
Microsoft Active Directory, JWT.

## Introduction

Spring Boot application that supports Token based Authentication with JWT for both PostgreSQL & Microsoft Active Directory. 

### Reference guide
For further reference, please consider the following sections:
* [spring-boot-jwt-authentication](https://bezkoder.com/spring-boot-jwt-authentication/)
* [active-directory-spring-sample](https://medium.com/@viraj.rajaguru/how-to-use-spring-security-to-authenticate-with-microsoft-active-directory-1caff11c57f2)
* [spring-security-multiple-auth-providers](https://www.baeldung.com/spring-security-multiple-auth-providers)
* [Setup Domain Controller in Windows Server 2016](https://www.youtube.com/watch?v=0LVn0h22tE8)

## Technology
* Java 8
* Spring Boot 2.2.6.RELEASE (with Spring Security, Spring Security LDAP,
  Spring Web, Spring Data JPA)
* jjwt 1.0.9.RELEASE - Java JWT: JSON Web Token for Java and Android
* PostgreSQL
* Gradle 6.3

## Run Spring Boot application
```bash
gradlew bootrun
```

### Run the SQL insert statement to populate the ROLES table
```sql
INSERT INTO roles(name) VALUES('ROLE_USER');
INSERT INTO roles(name) VALUES('ROLE_MODERATOR');
INSERT INTO roles(name) VALUES('ROLE_ADMIN');
```