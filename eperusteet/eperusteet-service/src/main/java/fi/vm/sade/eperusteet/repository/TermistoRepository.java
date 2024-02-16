package fi.vm.sade.eperusteet.repository;

import java.util.List;
import fi.vm.sade.eperusteet.domain.Termi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TermistoRepository extends JpaRepository<Termi, Long> {
    List<Termi> findByPerusteId(Long perusteId);

    Termi findByPerusteIdAndAvain(Long perusteId, String avain);
}
