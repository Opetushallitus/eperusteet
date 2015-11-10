package fi.vm.sade.eperusteet.repository;

import fi.vm.sade.eperusteet.domain.ammattitaitovaatimukset.AmmattitaitovaatimuksenKohdealue;
import fi.vm.sade.eperusteet.repository.version.JpaWithVersioningRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by autio on 21.10.2015.
 */
@Repository
public interface AmmattitaidonvaatimusRepository extends JpaWithVersioningRepository<AmmattitaitovaatimuksenKohdealue, Long> {
}
