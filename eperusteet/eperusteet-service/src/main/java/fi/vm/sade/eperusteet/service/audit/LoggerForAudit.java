package fi.vm.sade.eperusteet.service.audit;

import fi.vm.sade.auditlog.Logger;
import org.slf4j.LoggerFactory;

public class LoggerForAudit implements Logger {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(LoggerForAudit.class);

    @Override
    public void log(String msg) {
        LOGGER.info(msg);
    }

}
