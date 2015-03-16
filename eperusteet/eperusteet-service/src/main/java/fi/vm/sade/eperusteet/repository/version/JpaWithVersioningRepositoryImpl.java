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
package fi.vm.sade.eperusteet.repository.version;

import fi.vm.sade.eperusteet.domain.RevisionInfo;
import fi.vm.sade.eperusteet.domain.RevisionInfo_;
import fi.vm.sade.eperusteet.service.impl.PerusteenOsaServiceImpl;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.query.AuditEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;

public class JpaWithVersioningRepositoryImpl<T, ID extends Serializable> extends SimpleJpaRepository<T, ID> implements
    JpaWithVersioningRepository<T, ID> {

    private static final Logger LOG = LoggerFactory.getLogger(PerusteenOsaServiceImpl.class);

    private final EntityManager entityManager;
    private final JpaEntityInformation<T, ID> entityInformation;

    public JpaWithVersioningRepositoryImpl(JpaEntityInformation<T, ID> entityInformation, EntityManager entityManager) {
        super(entityInformation, entityManager);

        this.entityManager = entityManager;
        this.entityInformation = entityInformation;
    }

    @Override
    public List<Revision> getRevisions(ID id) {
        AuditReader auditReader = AuditReaderFactory.get(entityManager);

        @SuppressWarnings("unchecked")
        List<Object[]> results = (List<Object[]>) auditReader.createQuery()
            .forRevisionsOfEntity(entityInformation.getJavaType(), false, true)
            .addProjection(AuditEntity.revisionNumber())
            .addProjection(AuditEntity.revisionProperty(RevisionInfo_.timestamp.getName()))
            .addProjection(AuditEntity.revisionProperty(RevisionInfo_.muokkaajaOid.getName()))
            .addProjection(AuditEntity.revisionProperty(RevisionInfo_.kommentti.getName()))
            .addOrder(AuditEntity.revisionNumber().desc())
            .add(AuditEntity.id().eq(id))
            .getResultList();

        List<Revision> revisions = new ArrayList<>();
        for (Object[] result : results) {
            revisions.add(new Revision((Integer) result[0], (Long) result[1], (String) result[2], (String) result[3]));
        }

        return revisions;
    }

    @Override
    public T findRevision(ID id, Integer revisionId) {
        return AuditReaderFactory.get(entityManager).find(entityInformation.getJavaType(), id, revisionId);
    }

    @Override
    public Integer getLatestRevisionId(ID id) {
        AuditReader auditReader = AuditReaderFactory.get(entityManager);

        final Object result = auditReader.createQuery()
            .forRevisionsOfEntity(entityInformation.getJavaType(), false, true)
            .addProjection(AuditEntity.revisionNumber().max())
            .add(AuditEntity.id().eq(id))
            .getSingleResult();

        assert (result instanceof Number );
        return ((Number) result).intValue();
    }

    @Override
    public T lock(T entity) {
        return lock(entity, true);
    }

    @Override
    public T lock(T entity, boolean refresh) {
        if (refresh) {
            entityManager.refresh(entity, LockModeType.PESSIMISTIC_WRITE);
        } else {
            entityManager.lock(entity, LockModeType.PESSIMISTIC_WRITE);
        }
        return entity;
    }

    @Override
    public void setRevisioKommentti(String kommentti) {
        RevisionInfo currentRevision = AuditReaderFactory.get(entityManager).getCurrentRevision(RevisionInfo.class, false);
        currentRevision.addKommentti(kommentti);
    }

}
