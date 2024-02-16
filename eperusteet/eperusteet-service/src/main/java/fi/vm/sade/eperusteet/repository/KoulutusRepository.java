package fi.vm.sade.eperusteet.repository;

import fi.vm.sade.eperusteet.domain.Koulutus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KoulutusRepository extends JpaRepository<Koulutus, Long>{
    Koulutus findOneByKoulutuskoodiArvo(String koodi);
}
