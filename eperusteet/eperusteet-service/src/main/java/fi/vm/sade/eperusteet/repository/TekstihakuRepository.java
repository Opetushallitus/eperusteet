package fi.vm.sade.eperusteet.repository;

import fi.vm.sade.eperusteet.domain.tekstihaku.TekstiHaku;
import fi.vm.sade.eperusteet.domain.tekstihaku.TekstiHakuTulos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface TekstihakuRepository extends JpaRepository<TekstiHakuTulos, Long>, TekstihakuRepositoryCustom {
}
