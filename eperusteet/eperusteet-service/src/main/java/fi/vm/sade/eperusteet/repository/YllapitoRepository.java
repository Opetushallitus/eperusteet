package fi.vm.sade.eperusteet.repository;

import fi.vm.sade.eperusteet.domain.Yllapito;
import fi.vm.sade.eperusteet.repository.version.JpaWithVersioningRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface YllapitoRepository extends JpaWithVersioningRepository<Yllapito, Long> {
    Yllapito findById(Long id);
}
