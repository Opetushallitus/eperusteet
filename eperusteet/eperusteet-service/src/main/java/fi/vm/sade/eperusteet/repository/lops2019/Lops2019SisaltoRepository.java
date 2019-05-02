package fi.vm.sade.eperusteet.repository.lops2019;

import fi.vm.sade.eperusteet.domain.lops2019.Lops2019Sisalto;
import fi.vm.sade.eperusteet.repository.version.JpaWithVersioningRepository;

public interface Lops2019SisaltoRepository extends JpaWithVersioningRepository<Lops2019Sisalto, Long> {
    Lops2019Sisalto findByPerusteId(Long perusteId);
}
