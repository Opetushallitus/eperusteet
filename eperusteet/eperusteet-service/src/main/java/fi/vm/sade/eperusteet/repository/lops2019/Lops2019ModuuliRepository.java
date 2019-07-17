package fi.vm.sade.eperusteet.repository.lops2019;

import fi.vm.sade.eperusteet.domain.lops2019.oppiaineet.moduuli.Lops2019Moduuli;
import fi.vm.sade.eperusteet.repository.version.JpaWithVersioningRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Lops2019ModuuliRepository extends JpaWithVersioningRepository<Lops2019Moduuli, Long> {

}
