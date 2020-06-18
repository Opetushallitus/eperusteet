package fi.vm.sade.eperusteet.repository;

import fi.vm.sade.eperusteet.domain.TekstiKappale;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TekstikappaleRepository extends JpaRepository<TekstiKappale, Long> {
    
    List<TekstiKappale> findByKooditUri(String koodi);
}
