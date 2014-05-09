package fi.vm.sade.eperusteet.repository.version;

import fi.vm.sade.eperusteet.service.impl.PerusteenOsaServiceImpl;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.DefaultRevisionEntity;
import org.hibernate.envers.query.AuditEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;

public class JpaWithVersioningRepositoryImpl<T, ID extends Serializable> extends SimpleJpaRepository<T, ID> implements JpaWithVersioningRepository<T, ID> {

	private static final Logger LOG = LoggerFactory.getLogger(PerusteenOsaServiceImpl.class);

	private EntityManager entityManager;
	private JpaEntityInformation<T, ID> entityInformation;

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
				.addProjection(AuditEntity.revisionProperty("timestamp"))
				.addOrder(AuditEntity.revisionProperty("timestamp").desc())
				.add(AuditEntity.id().eq(id))
				.getResultList();

		List<Revision> revisions = new ArrayList<>();
		for (Object[] result : results) {
			revisions.add(new Revision((Integer) result[0], (Long) result[1]));
		}

		return revisions;
	}

	@Override
	public T findRevision(ID id, Integer revisionId) {
		return AuditReaderFactory.get(entityManager).find(entityInformation.getJavaType(), id, revisionId);
	}

	private static DefaultRevisionEntity getRevisionEntity(Object[] object) {
		return (DefaultRevisionEntity) object[1];
	}

}
