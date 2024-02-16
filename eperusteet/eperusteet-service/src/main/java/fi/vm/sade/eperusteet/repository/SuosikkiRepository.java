package fi.vm.sade.eperusteet.repository;

import fi.vm.sade.eperusteet.domain.Suosikki;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SuosikkiRepository extends JpaRepository<Suosikki, Long> {

}
