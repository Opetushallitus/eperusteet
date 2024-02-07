package fi.vm.sade.eperusteet.repository;

import fi.vm.sade.eperusteet.domain.yl.AIPEKurssi;
import fi.vm.sade.eperusteet.repository.version.JpaWithVersioningRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AIPEKurssiRepository extends JpaWithVersioningRepository<AIPEKurssi, Long> {

}
