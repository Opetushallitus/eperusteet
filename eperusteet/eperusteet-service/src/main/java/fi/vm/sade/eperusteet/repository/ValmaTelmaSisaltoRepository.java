package fi.vm.sade.eperusteet.repository;

import fi.vm.sade.eperusteet.domain.tutkinnonosa.ValmaTelmaSisalto;
import fi.vm.sade.eperusteet.repository.version.JpaWithVersioningRepository;

public interface ValmaTelmaSisaltoRepository extends JpaWithVersioningRepository<ValmaTelmaSisalto, Long> {
}
