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

import fi.vm.sade.auditlog.ApplicationType;
import fi.vm.sade.auditlog.Audit;
import fi.vm.sade.eperusteet.service.audit.LogMessage.LogMessageBuilder;
import fi.vm.sade.eperusteet.service.revision.RevisionMetaService;

import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * @author nkala
 */
@Component
public class EperusteetAudit {
    @Autowired
    private RevisionMetaService revisionMetaService;

    public static final Audit AUDIT = new Audit("eperusteet-service", ApplicationType.VIRKAILIJA);

    public static String username() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null
                ? auth.getName()
                : "Tuntematon käyttäjä";
    }

    public <T> T withAudit(LogMessageBuilder audit, Function<Void, T> fn) {
        audit.beforeRevision(revisionMetaService.getCurrentRevision());
        T result = fn.apply(null);
        audit.afterRevision(revisionMetaService.getCurrentRevision()).log();
        return result;
    }

    public void withAudit(LogMessageBuilder audit) {
        audit.afterRevision(revisionMetaService.getCurrentRevision()).log();
    }
}
