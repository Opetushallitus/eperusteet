package fi.vm.sade.eperusteet.repository;

import java.util.List;

import org.springframework.data.repository.NoRepositoryBean;

import fi.vm.sade.eperusteet.domain.audit.Revision;

@NoRepositoryBean
public interface PerusteenOsaRepositoryCustom {
	List<Revision> getRevisions(Long perusteenOsaId);
}
