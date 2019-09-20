package fi.vm.sade.eperusteet.service.audit;

import fi.vm.sade.auditlog.ApplicationType;
import fi.vm.sade.auditlog.Audit;
import fi.vm.sade.eperusteet.utils.audit.config.AuditUtilConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(AuditUtilConfiguration.class)
public class AuditConfiguration {

    @Bean
    public Audit audit() {
        return new Audit(new LoggerForAudit(), "eperusteet-service", ApplicationType.VIRKAILIJA);
    }

}
