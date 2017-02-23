/*
 * Copyright (c) 2013 The Finnish Board of Education - Opetushallitus
 *
 * This program is free software: Licensed under the EUPL, Version 1.1 or - as
 * soon as they will be approved by the European Commission - subsequent versions
 * of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at: http://ec.europa.eu/idabc/eupl
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * European Union Public Licence for more details.
 */

package fi.vm.sade.eperusteet.service.audit;

import fi.vm.sade.auditlog.AbstractLogMessage;
import fi.vm.sade.auditlog.SimpleLogMessageBuilder;
import java.util.Map;

/**
 *
 * @author nkala
 */
public class LogMessage extends AbstractLogMessage {
    public LogMessage(Map<String, String> messageMapping) {
        super(messageMapping);
    }

    public void setMap(Map<String, String> messageMapping) {
        getMessageMapping().clear();
        getMessageMapping().putAll(messageMapping);
    }

    public static LogMessageBuilder builder(Long perusteId, EperusteetMessageFields target, EperusteetOperation op) {
        LogMessageBuilder result = new LogMessageBuilder()
                .add("target", target.toString())
                .setOperation(op)
                .id(EperusteetAudit.username());

        if (perusteId != null) {
            result.add("peruste", perusteId.toString());
        }
        return result;
    }

    public static <T extends AuditLoggableDto> LogMessageBuilder builder(Long perusteId, EperusteetMessageFields target, EperusteetOperation op, T dto) {
        return builder(perusteId, target, op)
            .addDto(dto);
    }

    public static class LogMessageBuilder extends SimpleLogMessageBuilder<LogMessageBuilder> {
        private Number beforeRev;

        public LogMessage build() {
            return new LogMessage(mapping);
        }

        public void log() {
            EperusteetAudit.AUDIT.log(build());
        }

        public <T extends AuditLoggableDto> LogMessageBuilder addDto(T dto) {
            dto.auditLog(this);
            return this;
        }

        public LogMessageBuilder palautus(Long id, Long version) {
            return safePut("osaId", id.toString())
                .safePut("versio", id.toString());
        }

        public LogMessageBuilder setOperation(EperusteetOperation op) {
            return safePut("operation", op.toString());
        }

        public LogMessageBuilder beforeRevision(Number rev) {
            beforeRev = rev;
            return this;
        }

        public LogMessageBuilder afterRevision(Number rev) {
            if (beforeRev != null) {
                return add("rev", rev.toString(), beforeRev.toString());
            }
            else {
                return add("rev", rev.toString());
            }
        }
    }

}
