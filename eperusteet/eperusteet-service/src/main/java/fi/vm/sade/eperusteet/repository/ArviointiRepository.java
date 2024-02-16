package fi.vm.sade.eperusteet.repository;

import org.springframework.stereotype.Repository;

import fi.vm.sade.eperusteet.domain.arviointi.Arviointi;
import fi.vm.sade.eperusteet.repository.version.JpaWithVersioningRepository;

@Repository
public interface ArviointiRepository extends JpaWithVersioningRepository<Arviointi, Long> {

}
