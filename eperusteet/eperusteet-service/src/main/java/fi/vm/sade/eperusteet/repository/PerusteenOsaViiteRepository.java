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

    @Query(nativeQuery = true, value =
            "with recursive vanhemmat(id,vanhempi_id,perusteenosa_id) as "
            + "(select pv.id, pv.vanhempi_id, pv.perusteenosa_id from perusteenosaviite pv "
            + "where pv.perusteenosa_id = ?1 "
            + "union all "
            + "select pv.id, pv.vanhempi_id, v.perusteenosa_id "
            + "from perusteenosaviite pv, vanhemmat v where pv.id = v.vanhempi_id) "
            + "select CAST(id as BIGINT) from vanhemmat where vanhempi_id is null")
    List<Long> findRootsByPerusteenOsaId(Long perusteenOsaId);

    Long countByPerusteenOsaId(Long perusteenOsaId);
}
