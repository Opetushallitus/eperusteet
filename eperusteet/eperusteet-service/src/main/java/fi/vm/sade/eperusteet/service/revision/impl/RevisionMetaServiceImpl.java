/*
 * Copyright (c) 2016 The Finnish Board of Education - Opetushallitus
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

package fi.vm.sade.eperusteet.service.revision.impl;

import fi.vm.sade.eperusteet.service.revision.RevisionMetaService;
import java.util.Date;
import javax.persistence.EntityManager;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author nkala
 */
@Service
@Transactional
public class RevisionMetaServiceImpl implements RevisionMetaService {
    @Autowired
    private EntityManager em;

    @Override
    public Number getCurrentRevision() {
        AuditReader reader = AuditReaderFactory.get(em);
        Number revision = reader.getRevisionNumberForDate(new Date(Long.MAX_VALUE));
        return revision;
    }

}
