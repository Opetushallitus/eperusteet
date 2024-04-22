package fi.vm.sade.eperusteet.service.test;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.IfProfileValue;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Kantaluokka service-tason integraatiotesteille joita ajetaan "oikeaa" tietokantaa vasten.
 * Vaatii paikallisen asennuksen sovelluksen tietokannasta. Tämän takia testit ovat ehdollisia.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestConfiguration.class)
@IfProfileValue(name="db-it-tests", values="true")
public class AbstractDbIntegrationTest {

    @Before
    public void setUpSecurityContext() {
        SecurityContext ctx = SecurityContextHolder.createEmptyContext();
        ctx.setAuthentication(new UsernamePasswordAuthenticationToken("test","test"));
        SecurityContextHolder.setContext(ctx);

        // PerusteUpdateStoreImpl @scope:n takia
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

}
