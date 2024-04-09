package fi.vm.sade.eperusteet.config;

import fi.vm.sade.eperusteet.repository.OphSessionMappingStorage;
import fi.vm.sade.eperusteet.repository.custom.OphSessionMappingStorageImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.session.SessionRepository;
import org.springframework.session.jdbc.config.annotation.web.http.EnableJdbcHttpSession;
import org.springframework.session.web.context.AbstractHttpSessionApplicationInitializer;

@Profile("!test")
@Configuration
@EnableJdbcHttpSession
public class SessionConfig extends AbstractHttpSessionApplicationInitializer {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private SessionRepository sessionRepository;

    @Bean
    public OphSessionMappingStorage sessionMappingStorage() {
        return new OphSessionMappingStorageImpl(jdbcTemplate, sessionRepository);
    }
}
