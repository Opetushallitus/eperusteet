package fi.vm.sade.eperusteet.repository;

import fi.vm.sade.eperusteet.domain.yl.LaajaalainenOsaaminen;
import fi.vm.sade.eperusteet.repository.version.JpaWithVersioningRepository;
import org.springframework.data.jpa.repository.Query;

public interface LaajaalainenOsaaminenRepository extends JpaWithVersioningRepository<LaajaalainenOsaaminen, Long> {
    @Query("SELECT l from PerusopetuksenPerusteenSisalto s JOIN s.laajaalaisetosaamiset l where s.peruste.id = ?1 and l.id = ?2")
    LaajaalainenOsaaminen findBy(Long perusteId, Long id);
}
