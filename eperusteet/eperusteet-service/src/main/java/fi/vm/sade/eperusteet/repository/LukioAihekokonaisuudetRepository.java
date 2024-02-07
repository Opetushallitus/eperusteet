package fi.vm.sade.eperusteet.repository;

import fi.vm.sade.eperusteet.domain.yl.lukio.Aihekokonaisuudet;
import fi.vm.sade.eperusteet.repository.version.JpaWithVersioningRepository;

public interface LukioAihekokonaisuudetRepository extends JpaWithVersioningRepository<Aihekokonaisuudet, Long> {

}
