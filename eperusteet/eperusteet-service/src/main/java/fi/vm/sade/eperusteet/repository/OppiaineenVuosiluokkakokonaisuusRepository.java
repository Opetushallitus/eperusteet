package fi.vm.sade.eperusteet.repository;

import fi.vm.sade.eperusteet.domain.yl.OppiaineenVuosiluokkaKokonaisuus;
import fi.vm.sade.eperusteet.repository.version.JpaWithVersioningRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OppiaineenVuosiluokkakokonaisuusRepository extends JpaWithVersioningRepository<OppiaineenVuosiluokkaKokonaisuus, Long> {

    OppiaineenVuosiluokkaKokonaisuus findByIdAndOppiaineId(Long id, Long oppiaineId);
}
