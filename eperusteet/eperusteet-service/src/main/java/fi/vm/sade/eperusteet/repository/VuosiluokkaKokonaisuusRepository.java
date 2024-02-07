package fi.vm.sade.eperusteet.repository;

import fi.vm.sade.eperusteet.domain.yl.VuosiluokkaKokonaisuus;
import fi.vm.sade.eperusteet.repository.version.JpaWithVersioningRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VuosiluokkaKokonaisuusRepository extends JpaWithVersioningRepository<VuosiluokkaKokonaisuus, Long> {

}
