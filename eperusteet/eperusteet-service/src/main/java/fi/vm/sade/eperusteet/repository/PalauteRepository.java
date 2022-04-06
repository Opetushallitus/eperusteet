package fi.vm.sade.eperusteet.repository;

import fi.vm.sade.eperusteet.domain.Palaute;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PalauteRepository extends JpaRepository<Palaute, Palaute.Key> {

    List<Palaute> findByKey(String key);
}
