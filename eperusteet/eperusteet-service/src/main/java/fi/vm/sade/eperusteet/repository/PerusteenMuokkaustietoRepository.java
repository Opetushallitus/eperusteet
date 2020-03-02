package fi.vm.sade.eperusteet.repository;

import fi.vm.sade.eperusteet.domain.PerusteenMuokkaustieto;
import java.util.Date;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PerusteenMuokkaustietoRepository extends JpaRepository<PerusteenMuokkaustieto, Long> {

    List<PerusteenMuokkaustieto> findByPerusteIdAndLuotuBeforeOrderByLuotuDesc(Long opsId, Date viimeisinLuontiaika, Pageable pageable);

    default List<PerusteenMuokkaustieto> findTop10ByPerusteIdAndLuotuBeforeOrderByLuotuDesc(Long opsId, Date viimeisinLuontiaika, int lukumaara) {
        return findByPerusteIdAndLuotuBeforeOrderByLuotuDesc(opsId, viimeisinLuontiaika, new PageRequest(0, Math.min(lukumaara, 100)));
    }

    List<PerusteenMuokkaustieto> findByKohdeId(Long kohdeId);
}
