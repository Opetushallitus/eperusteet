package fi.vm.sade.eperusteet.repository;

import fi.vm.sade.eperusteet.domain.PerusteenOsa;
import fi.vm.sade.eperusteet.domain.PerusteenOsaViite;
import fi.vm.sade.eperusteet.repository.version.JpaWithVersioningRepository;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PerusteenOsaViiteRepository extends JpaWithVersioningRepository<PerusteenOsaViite, Long> {

    List<PerusteenOsaViite> findAllByPerusteenOsa(PerusteenOsa perusteenOsa);

    @Query(name = "PerusteenOsaViite.findRootsByPerusteenOsaId")
    List<Long> findRootsByPerusteenOsaId(Long perusteenOsaId);

    Long countByPerusteenOsaId(Long perusteenOsaId);
}
