package fi.vm.sade.eperusteet.repository;

import fi.vm.sade.eperusteet.domain.maarays.Maarays;
import fi.vm.sade.eperusteet.domain.maarays.MaaraysAsiasanatFetch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MaaraysAsiasanaRepository extends JpaRepository<Maarays, Long> {

    List<MaaraysAsiasanatFetch> findAllBy();
}
