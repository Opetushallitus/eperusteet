package fi.vm.sade.eperusteet.repository.custom;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.vm.sade.eperusteet.domain.PerusteenOsa;
import fi.vm.sade.eperusteet.domain.audit.Revision;
import fi.vm.sade.eperusteet.repository.PerusteenOsaRepositoryCustom;

public class PerusteenOsaRepositoryImpl implements PerusteenOsaRepositoryCustom {

	@SuppressWarnings("unused")
	private static final Logger LOG = LoggerFactory.getLogger(PerusteenOsaRepositoryImpl.class);
	
	@PersistenceContext
	private EntityManager entityManager;
	
	@Override
	public List<Revision> getRevisions(Long perusteenOsaId) {
		AuditReader auditReader = AuditReaderFactory.get(entityManager);
		List<Number> revisionNumbers = auditReader.getRevisions(PerusteenOsa.class, perusteenOsaId);
		
		List<Revision> revisions = new ArrayList<>();
		for(Number number : revisionNumbers) {
			revisions.add(new Revision(number.longValue(), auditReader.getRevisionDate(number)));
		}
		return revisions;
	}

}
