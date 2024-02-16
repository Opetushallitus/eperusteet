package fi.vm.sade.eperusteet.repository;

import fi.vm.sade.eperusteet.domain.ammattitaitovaatimukset.AmmattitaitovaatimuksenKohdealue;
import fi.vm.sade.eperusteet.repository.version.JpaWithVersioningRepository;
import org.springframework.stereotype.Repository;

@Repository
@Deprecated
public interface AmmattitaidonvaatimusRepository extends JpaWithVersioningRepository<AmmattitaitovaatimuksenKohdealue, Long> {
}
