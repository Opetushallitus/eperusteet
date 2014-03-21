package fi.vm.sade.eperusteet.repository.custom;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.DefaultRevisionEntity;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.hibernate.envers.query.criteria.AuditConjunction;
import org.hibernate.envers.query.criteria.AuditDisjunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import fi.vm.sade.eperusteet.domain.Arviointi;
import fi.vm.sade.eperusteet.domain.PerusteenOsa;
import fi.vm.sade.eperusteet.domain.TutkinnonOsa;
import fi.vm.sade.eperusteet.domain.audit.Revision;
import fi.vm.sade.eperusteet.repository.PerusteenOsaRepositoryCustom;

public class PerusteenOsaRepositoryImpl implements PerusteenOsaRepositoryCustom {

	private static final Logger LOG = LoggerFactory.getLogger(PerusteenOsaRepositoryImpl.class);

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public List<Revision> getRevisions(Long perusteenOsaId) {
		AuditReader auditReader = AuditReaderFactory.get(entityManager);

		@SuppressWarnings("unchecked")
		List<Object[]> results = (List<Object[]>) auditReader.createQuery()
				.forRevisionsOfEntity(PerusteenOsa.class, false, true)
				.addProjection(AuditEntity.revisionNumber())
				.addProjection(AuditEntity.revisionProperty("timestamp"))
				.addOrder(AuditEntity.revisionProperty("timestamp").desc())
				.add(AuditEntity.id().eq(perusteenOsaId)).getResultList();

		List<Revision> revisions = new ArrayList<>();
		for (Object[] result : results) {
			revisions.add(new Revision((Integer) result[0], (Long) result[1]));
		}

		return revisions;
	}

	@Override
	public PerusteenOsa findRevision(Long perusteenOsaId, Integer revisionId) {
		return AuditReaderFactory.get(entityManager).find(PerusteenOsa.class, perusteenOsaId, revisionId);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Revision> getNestedRevisions(TutkinnonOsa tutkinnonOsa) {
		LOG.debug("Find revisions with nested changes");
		AuditReader auditReader = AuditReaderFactory.get(entityManager);

		Set<Revision> revisionSet = new HashSet<>();

		List<Object[]> tutkinnonOsaRevisions = (List<Object[]>) auditReader.createQuery()
				.forRevisionsOfEntity(TutkinnonOsa.class, false, true)
				.addOrder(AuditEntity.revisionProperty("timestamp").asc())
				.add(AuditEntity.id().eq(tutkinnonOsa.getId())).getResultList();

		LOG.debug("Perus tutkinnon osa -revisiot");
		printListWithArrays(tutkinnonOsaRevisions);
		addRevisionsToSet(revisionSet, tutkinnonOsaRevisions);

		if (tutkinnonOsaRevisions != null && tutkinnonOsaRevisions.size() > 0) {
			AuditQuery arviointiIdChangedQuery = auditReader.createQuery()
					.forRevisionsOfEntity(TutkinnonOsa.class, false, true)
					.addProjection(AuditEntity.revisionNumber())
					.addProjection(AuditEntity.property("arviointi_id"))
					.addOrder(AuditEntity.revisionProperty("timestamp").asc())
					.add(AuditEntity.revisionNumber().ge(getRevisionEntity(tutkinnonOsaRevisions.get(0)).getId()))
					.add(AuditEntity.property("arviointi").hasChanged());
			
			if (RevisionType.DEL.equals(tutkinnonOsaRevisions.get(tutkinnonOsaRevisions.size() - 1)[2])) {
				arviointiIdChangedQuery.add(AuditEntity.revisionNumber().lt(
						getRevisionEntity(tutkinnonOsaRevisions.get(tutkinnonOsaRevisions.size() - 1)).getId()));
			}

			List<Object[]> revisionsWhereArviointiIdChanged = arviointiIdChangedQuery.getResultList();

			LOG.debug("revisiot missä arviointi-id oli muuttunut");
			printListWithArrays(revisionsWhereArviointiIdChanged);

			AuditQuery arviointiChangedQuery = auditReader.createQuery()
					.forRevisionsOfEntity(Arviointi.class, false, true)
					.addOrder(AuditEntity.revisionProperty("timestamp").asc());

			AuditDisjunction disjunction = AuditEntity.disjunction();

			ListIterator<Object[]> listIterator = revisionsWhereArviointiIdChanged.listIterator();
			while (listIterator.hasNext()) {
				Object[] currentRevision = listIterator.next();

				AuditConjunction conjunction = AuditEntity.conjunction()
						.add(AuditEntity.id().eq(currentRevision[1]))
						.add(AuditEntity.revisionNumber().ge((Number) currentRevision[0]));

				if (listIterator.hasNext()) {
					Object[] nextRevision = revisionsWhereArviointiIdChanged.get(listIterator.nextIndex());
					conjunction.add(AuditEntity.revisionNumber().lt((Number) nextRevision[0]));
				}

				disjunction.add(conjunction);
			}

			arviointiChangedQuery.add(disjunction);

			List<Object[]> arviointiRevisions = arviointiChangedQuery.getResultList();

			LOG.debug("arviointirevisiot");
			printListWithArrays(arviointiRevisions);
			addRevisionsToSet(revisionSet, arviointiRevisions);
		}

		LOG.debug("return revisions");
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
