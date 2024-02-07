package fi.vm.sade.eperusteet.repository;

import fi.vm.sade.eperusteet.domain.yl.OpetuksenKohdealue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OpetuksenKohdeAlueRepository extends JpaRepository<OpetuksenKohdealue, Long> {

}
