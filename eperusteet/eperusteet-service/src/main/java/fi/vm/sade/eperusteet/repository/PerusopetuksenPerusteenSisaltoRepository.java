package fi.vm.sade.eperusteet.repository;

import fi.vm.sade.eperusteet.domain.yl.PerusopetuksenPerusteenSisalto;
import fi.vm.sade.eperusteet.repository.version.JpaWithVersioningRepository;

public interface PerusopetuksenPerusteenSisaltoRepository extends JpaWithVersioningRepository<PerusopetuksenPerusteenSisalto,Long>,
        OppiaineSisaltoRepository<PerusopetuksenPerusteenSisalto> {
}
