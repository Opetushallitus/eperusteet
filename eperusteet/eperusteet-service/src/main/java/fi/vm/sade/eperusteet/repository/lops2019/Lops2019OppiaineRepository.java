package fi.vm.sade.eperusteet.repository.lops2019;

import fi.vm.sade.eperusteet.domain.lops2019.oppiaineet.Lops2019Oppiaine;
import fi.vm.sade.eperusteet.repository.version.JpaWithVersioningRepository;

public interface Lops2019OppiaineRepository
        extends JpaWithVersioningRepository<Lops2019Oppiaine, Long> {
}
