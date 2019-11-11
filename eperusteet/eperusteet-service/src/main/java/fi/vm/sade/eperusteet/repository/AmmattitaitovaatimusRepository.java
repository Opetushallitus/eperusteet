package fi.vm.sade.eperusteet.repository;

import fi.vm.sade.eperusteet.domain.tutkinnonosa.Ammattitaitovaatimus2019;
import fi.vm.sade.eperusteet.repository.version.JpaWithVersioningRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AmmattitaitovaatimusRepository extends JpaWithVersioningRepository<Ammattitaitovaatimus2019, Long>, AmmattitaitovaatimusRepositoryCustom {
}
