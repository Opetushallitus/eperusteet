package fi.vm.sade.eperusteet.repository.lops2019;

import fi.vm.sade.eperusteet.domain.lops2019.laajaalainenosaaminen.Lops2019LaajaAlainenOsaaminen;
import fi.vm.sade.eperusteet.repository.version.JpaWithVersioningRepository;

public interface Lops2019LaajaAlainenRepository
        extends JpaWithVersioningRepository<Lops2019LaajaAlainenOsaaminen, Long> {

}
