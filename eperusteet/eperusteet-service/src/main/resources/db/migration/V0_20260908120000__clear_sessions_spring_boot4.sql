-- Spring Security 7 removed org.springframework.security.cas.web.authentication.ServiceAuthenticationDetails.
-- Sessions serialized by Spring Boot 3 / Security 6 cannot be read and cause ConversionFailedException
-- on every request. Users must re-authenticate after this one-time cleanup.
TRUNCATE TABLE cas_client_session;
TRUNCATE TABLE spring_session CASCADE;
