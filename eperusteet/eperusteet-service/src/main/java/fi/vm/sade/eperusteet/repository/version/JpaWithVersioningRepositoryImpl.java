package fi.vm.sade.eperusteet.repository.version;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import javax.persistence.EntityManager;

import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.DefaultRevisionEntity;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.hibernate.envers.query.criteria.AuditConjunction;
import org.hibernate.envers.query.criteria.AuditDisjunction;
import org.jgroups.annotations.Experimental;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;

import com.google.common.collect.Lists;

import fi.vm.sade.eperusteet.service.impl.PerusteenOsaServiceImpl;

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

	@Override
	@SuppressWarnings("unchecked")
	@Experimental(comment = "Toteutettu tällä hetkellä testimielessä")
	public List<Revision> getRevisions(ID id, String... childPaths) {
		LOG.debug("Find revisions with nested changes");
		AuditReader auditReader = AuditReaderFactory.get(entityManager);

		Set<Revision> revisionSet = new HashSet<>();

		List<Object[]> tutkinnonOsaRevisions = (List<Object[]>) auditReader.createQuery()
				.forRevisionsOfEntity(entityInformation.getJavaType(), false, true)
				.addOrder(AuditEntity.revisionProperty("timestamp").asc())
				.add(AuditEntity.id().eq(id))
				.getResultList();

		LOG.debug("Perus tutkinnon osa -revisiot");
		printListWithArrays(tutkinnonOsaRevisions);
		addRevisionsToSet(revisionSet, tutkinnonOsaRevisions);

		if (tutkinnonOsaRevisions != null && tutkinnonOsaRevisions.size() > 0) {
			for (String childPath : childPaths) {
				LOG.debug("current path: {}", childPath);
				if (childPath.split(".").length > 1) {
					throw new InvalidChildPathException("current implementation does not allow nested paths");
				}

				AuditQuery childIdChangedQuery = auditReader.createQuery()
						.forRevisionsOfEntity(entityInformation.getJavaType(), false, true)
						.addProjection(AuditEntity.revisionNumber())
						.addProjection(AuditEntity.property(childPath + "_id"))
						.addOrder(AuditEntity.revisionProperty("timestamp").asc())
						.add(AuditEntity.revisionNumber().ge(getRevisionEntity(tutkinnonOsaRevisions.get(0)).getId()))
						.add(AuditEntity.property(childPath).hasChanged());
				
				 if (RevisionType.DEL.equals(tutkinnonOsaRevisions.get(tutkinnonOsaRevisions.size() - 1)[2])) {
					 childIdChangedQuery.add(AuditEntity.revisionNumber().lt(getRevisionEntity(tutkinnonOsaRevisions.get(tutkinnonOsaRevisions.size() - 1)).getId()));
				 }
				
				 List<Object[]> revisionsWhereArviointiIdChanged = childIdChangedQuery.getResultList();
				 
				 printListWithArrays(revisionsWhereArviointiIdChanged);
				 
				 LOG.debug("entity type: {}", entityInformation.getJavaType());
				 LOG.debug("property type: {}", BeanUtils.findPropertyType(childPath, entityInformation.getJavaType()));
				 AuditQuery childChangedQuery = auditReader.createQuery()
						 .forRevisionsOfEntity(BeanUtils.findPropertyType(childPath, entityInformation.getJavaType()), false, true)
						 .addOrder(AuditEntity.revisionProperty("timestamp").asc());
					
				 AuditDisjunction disjunction = AuditEntity.disjunction();
					
				 ListIterator<Object[]> listIterator = revisionsWhereArviointiIdChanged.listIterator();
				 while (listIterator.hasNext()) {
					 Object[] currentRevision = listIterator.next();
					
					 AuditConjunction conjunction = AuditEntity.conjunction()
							 .add(AuditEntity.id().eq(currentRevision[1]))
							 .add(AuditEntity.revisionNumber().ge((Number) currentRevision[0]));
					
					 if (listIterator.hasNext()) {
						 Object[] nextRevision =
						 revisionsWhereArviointiIdChanged.get(listIterator.nextIndex());
						 conjunction.add(AuditEntity.revisionNumber().lt((Number)
						 nextRevision[0]));
					 }
					
					 disjunction.add(conjunction);
				 }
				 
				 childChangedQuery.add(disjunction);
				 
				 List<Object[]> childRevisions = childChangedQuery.getResultList();
				 addRevisionsToSet(revisionSet, childRevisions);
			}
		}

		return Lists.newArrayList(revisionSet.iterator());
	}

	private static DefaultRevisionEntity getRevisionEntity(Object[] object) {
		return (DefaultRevisionEntity) object[1];
	}

	private static void addRevisionsToSet(Set<Revision> revisionSet, List<Object[]> revisions) {
		for (Object[] currentRevision : revisions) {
			DefaultRevisionEntity revisionEntity = getRevisionEntity(currentRevision);
			revisionSet.add(new Revision(revisionEntity.getId(), revisionEntity.getTimestamp()));
		}
	}

	private static void printListWithArrays(List<Object[]> list) {
		StringBuilder sb = new StringBuilder();
		sb.append('[');

		for (Object[] array : list) {
			sb.append('{');
			for (Object current : array) {
				sb.append(current).append(',');
			}
			sb.setCharAt(sb.length() - 1, '}');
			sb.append(',');
		}
		sb.setCharAt(sb.length() - 1, ']');
		LOG.debug(sb.toString());
	}
}
