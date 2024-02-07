package fi.vm.sade.eperusteet.service.impl;

import fi.vm.sade.eperusteet.domain.RevisionInfo;
import fi.vm.sade.eperusteet.service.util.SecurityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuditRevisionListener implements org.hibernate.envers.RevisionListener {
    private static final Logger LOG = LoggerFactory.getLogger(AuditRevisionListener.class);

    @Override
    public void newRevision(Object revisionEntity) {
        if ( revisionEntity instanceof RevisionInfo ) {
            RevisionInfo ri = (RevisionInfo)revisionEntity;
            ri.setMuokkaajaOid(SecurityUtil.getAuthenticatedPrincipal().getName());
        }
    }

}
