package fi.vm.sade.eperusteet.service.test;

import fi.vm.sade.eperusteet.utils.client.OphClientHelper;
import org.flywaydb.core.Flyway;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.PermissionEvaluator;

import javax.sql.DataSource;

@Configuration
@ImportResource({"classpath:it-test-context.xml", "classpath:it-docker-test-context.xml"})
public class TestConfiguration {

    @Autowired
    private DataSource dataSource;

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

    @Bean(initMethod = "migrate")
    public Flyway flyway() {
        Flyway flyway = Flyway.configure()
                .baselineOnMigrate(true)
                .dataSource(dataSource)
                .outOfOrder(true)
                .load();
        return flyway;
    }

}
