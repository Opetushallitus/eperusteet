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

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fi.vm.sade.auditlog.AbstractLogMessage;
import fi.vm.sade.auditlog.SimpleLogMessageBuilder;
import fi.vm.sade.eperusteet.service.util.Pair;
import java.util.ArrayList;
import java.util.List;
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
                .addTarget("tyyppi", target)
                .addTarget("peruste", perusteId)
                .setOperation(op)
                .id(EperusteetAudit.username());
        return result;
    }

    public void log() {
        EperusteetAudit.AUDIT.log(this);
    }

    public static class LogMessageBuilder extends SimpleLogMessageBuilder<LogMessageBuilder> {
        private Long beforeRev;
        private Long afterRev;
        private List<Pair<String, String>> targets = new ArrayList<>();

        public LogMessage build(EperusteetAudit audit) {
            JsonNodeFactory nodeFactory = new JsonNodeFactory(false);

            // FIXME: auditlogger syö vain merkkijonoja
            { // Changes
                ObjectNode changes = nodeFactory.objectNode();
                changes.put("rev", nodeFactory.objectNode()
                    .put("oldValue", beforeRev)
                    .put("newValue", afterRev));
                mapping.put("changes", changes.toString());
            }

            { // Target
                ObjectNode targetsObj = nodeFactory.objectNode();
                for (Pair<String, String> target : targets) {
                    targetsObj.put(target.getFirst(), target.getSecond());
                }
                mapping.put("target", targetsObj.toString());
            }

            { // User
                mapping.put("user", audit.getLoggableUser().toString());
            }

            return new LogMessage(mapping);
        }

        public LogMessageBuilder palautus(Long id, Long version) {
            return safePut("osaId", id.toString())
                .safePut("versio", id.toString());
        }

        public LogMessageBuilder addTarget(String name, Object op) {
            if (op != null) {
                targets.add(Pair.of(name, op.toString()));
            }
            return this;
        }

        public LogMessageBuilder setOperation(EperusteetOperation op) {
            return safePut("operation", op.toString());
        }

        public LogMessageBuilder beforeRevision(Number rev) {
            if (rev != null) {
                this.beforeRev = rev.longValue();
            }
            else {
                this.beforeRev = 0L;
            }
            return this;
        }

        public LogMessageBuilder afterRevision(Number rev) {
            if (rev != null) {
                this.afterRev = rev.longValue();
            }
            else {
                this.afterRev = 0L;
            }
            return this;
        }
    }

}
