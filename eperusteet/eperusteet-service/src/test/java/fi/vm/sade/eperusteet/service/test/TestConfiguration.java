package fi.vm.sade.eperusteet.service.test;

import fi.vm.sade.eperusteet.utils.client.OphClientHelper;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.PermissionEvaluator;

@Configuration
@ImportResource("classpath:it-test-context.xml")
public class TestConfiguration {

    @Profile("realPermissions")
    @Bean("testPermissionEvaluator")
    public PermissionEvaluator testPermissionEvaluator() {
        return new fi.vm.sade.eperusteet.service.security.PermissionEvaluator();
    }

    @Profile("!realPermissions")
    @Bean("testPermissionEvaluator")
    public PermissionEvaluator testPermissionEvaluatorMocked() {
        return new TestPermissionEvaluator();
    }

    @Bean
    public OphClientHelper ophClientHelper() {
        return Mockito.mock(OphClientHelper.class);
    }
}
