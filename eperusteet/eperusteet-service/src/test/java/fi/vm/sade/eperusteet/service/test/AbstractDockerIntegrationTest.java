package fi.vm.sade.eperusteet.service.test;

import fi.vm.sade.eperusteet.service.test.util.PerusteprojektiTestUtils;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Julkaisutestejä varten tehty ja käyttää eri profiileja kuin muut testit.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestConfiguration.class)
@ActiveProfiles(profiles = {"docker", "default"})
public class AbstractDockerIntegrationTest {

    @Autowired
    public PerusteprojektiTestUtils ppTestUtils;

    @Before
    public void setUpSecurityContext() {
        SecurityContext ctx = SecurityContextHolder.createEmptyContext();
        ctx.setAuthentication(new UsernamePasswordAuthenticationToken("test", "test"));
        SecurityContextHolder.setContext(ctx);

        // PerusteUpdateStoreImpl @scope:n takia
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    public void loginAsUser(String user) {
        SecurityContext ctx = SecurityContextHolder.createEmptyContext();
        ctx.setAuthentication(new UsernamePasswordAuthenticationToken(user,"test"));
        SecurityContextHolder.setContext(ctx);
    }

    public void invalidateAuthentication() {
        SecurityContextHolder.clearContext();
    }

}
