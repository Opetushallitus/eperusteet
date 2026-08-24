package fi.vm.sade.eperusteet.service.test;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;

import java.util.List;

/**
 * Two-arg {@link UsernamePasswordAuthenticationToken} is unauthenticated in Spring Security 7.
 * Authorities match {@code it-test-context.xml} in-memory users.
 */
public final class TestUser {

    private TestUser() {
    }

    public static UsernamePasswordAuthenticationToken authenticated(String username) {
        return new UsernamePasswordAuthenticationToken(username, "test", authoritiesFor(username));
    }

    private static List<GrantedAuthority> authoritiesFor(String username) {
        if ("testOphAdmin".equals(username)) {
            return AuthorityUtils.createAuthorityList(
                    "ROLE_ADMIN",
                    "ROLE_USER",
                    "ROLE_APP_EPERUSTEET",
                    "ROLE_APP_EPERUSTEET_ADMIN_1.2.246.562.10.00000000001",
                    "ROLE_APP_EPERUSTEET_CRUD",
                    "ROLE_APP_EPERUSTEET_READ_UPDATE",
                    "ROLE_APP_EPERUSTEET_READ_UPDATE_1.2.246.562.10.00000000001",
                    "ROLE_APP_EPERUSTEET_CRUD_1.2.246.562.10.00000000001");
        }
        if ("test".equals(username)) {
            return AuthorityUtils.createAuthorityList(
                    "ROLE_ADMIN",
                    "ROLE_USER",
                    "ROLE_APP_EPERUSTEET",
                    "ROLE_APP_EPERUSTEET_CRUD",
                    "ROLE_APP_EPERUSTEET_READ_UPDATE",
                    "ROLE_APP_EPERUSTEET_READ_UPDATE_1.2.246.562.10.00000000001",
                    "ROLE_APP_EPERUSTEET_CRUD_1.2.246.562.10.00000000001");
        }
        return AuthorityUtils.createAuthorityList("ROLE_USER");
    }
}
