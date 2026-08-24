package fi.vm.sade.eperusteet.service.impl;

import fi.vm.sade.eperusteet.domain.RevisionInfo;
import fi.vm.sade.eperusteet.repository.version.RevisioKommenttiHolder;
import fi.vm.sade.eperusteet.service.util.SecurityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Principal;

public class AuditRevisionListener implements org.hibernate.envers.RevisionListener {
    private static final Logger LOG = LoggerFactory.getLogger(AuditRevisionListener.class);

    @Override
    public void newRevision(Object revisionEntity) {
        if (revisionEntity instanceof RevisionInfo) {
            RevisionInfo ri = (RevisionInfo) revisionEntity;
            Principal principal = SecurityUtil.getAuthenticatedPrincipal();
            ri.setMuokkaajaOid(principal != null ? principal.getName() : "tuntematon");
            String kommentti = RevisioKommenttiHolder.poll();
            if (kommentti != null) {
                ri.addKommentti(kommentti);
            }
        }
    }

}
