package fi.vm.sade.eperusteet.repository;

import fi.vm.sade.eperusteet.domain.KVLiite;
import fi.vm.sade.eperusteet.repository.version.JpaWithVersioningRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KVLiiteRepository extends JpaWithVersioningRepository<KVLiite, Long> {

}
