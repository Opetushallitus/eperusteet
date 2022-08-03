package fi.vm.sade.eperusteet.repository;

import fi.vm.sade.eperusteet.domain.TekstiKappale;
import fi.vm.sade.eperusteet.repository.version.JpaWithVersioningRepository;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TekstikappaleRepository extends JpaWithVersioningRepository<TekstiKappale, Long> {

    List<TekstiKappale> findByKooditUri(String koodi);
}
