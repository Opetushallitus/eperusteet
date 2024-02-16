package fi.vm.sade.eperusteet.repository.version;

import fi.vm.sade.eperusteet.domain.RevisionInfo;
import fi.vm.sade.eperusteet.domain.RevisionInfo_;
import fi.vm.sade.eperusteet.service.impl.PerusteenOsaServiceImpl;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.query.AuditEntity;
import org.joda.time.DateTime;
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
        AuditReader auditReader = AuditReaderFactory.get(entityManager);
        T t = auditReader.find(entityInformation.getJavaType(), id, revisionId);
        return t;
    }

    @Override
    public Revision getLatestRevisionId(ID id) {
        AuditReader auditReader = AuditReaderFactory.get(entityManager);

        final Object result = auditReader.createQuery()
            .forRevisionsOfEntity(entityInformation.getJavaType(), false, true)
            .addProjection(AuditEntity.revisionNumber().max())
            .add(AuditEntity.id().eq(id))
            .getSingleResult();

        if (result == null) {
            return null;
        }
        assert (result instanceof Number);
        RevisionInfo rev = auditReader.findRevision(RevisionInfo.class, ((Number) result));
        return new Revision(rev.getId(), rev.getTimestamp(), rev.getMuokkaajaOid(), rev.getKommentti());
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

    /**
     * Palauttaa viimeisimmän versionumeron
     */
    @Override
    public int getLatestRevisionId() {
        //enverssissä ei ole suoraan suurimman revisionumeron palautusta, workaround
        return AuditReaderFactory.get(entityManager).getRevisionNumberForDate(DateTime.now().plusDays(1).toDate()).intValue();
    }

    @Override
    public T getLatestNotNull(ID id) {
        List<Revision> revisions = this.getRevisions(id);
        revisions = revisions.stream()
                .sorted(Comparator.comparing(Revision::getPvm).reversed())
                .collect(Collectors.toList());

        for (Revision revision : revisions) {
            T last = this.findRevision(id, revision.getNumero());
            if (last != null) {
                return last;
            }
        }
        return null;
    }

}
