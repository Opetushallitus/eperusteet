package fi.vm.sade.eperusteet.repository;

import fi.vm.sade.eperusteet.domain.Yllapito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface YllapitoRepository extends JpaRepository<Yllapito, Long> {
    Yllapito findByKey(String key);
}
