package fi.vm.sade.eperusteet.repository;

import fi.vm.sade.eperusteet.domain.yl.lukio.OpetuksenYleisetTavoitteet;
import fi.vm.sade.eperusteet.repository.version.JpaWithVersioningRepository;

public interface LukioYleisetTavoitteetRepository extends JpaWithVersioningRepository<OpetuksenYleisetTavoitteet, Long> {

}
