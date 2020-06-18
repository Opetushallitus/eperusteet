package fi.vm.sade.eperusteet.repository;

import fi.vm.sade.eperusteet.domain.Yllapito;
import fi.vm.sade.eperusteet.repository.version.JpaWithVersioningRepository;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public interface YllapitoRepository extends JpaWithVersioningRepository<Yllapito, Long> {

    List<Yllapito> findBySallittu(boolean sallittu);
}
