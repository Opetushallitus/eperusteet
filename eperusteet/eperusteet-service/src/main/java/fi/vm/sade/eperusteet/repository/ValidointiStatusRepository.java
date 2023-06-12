package fi.vm.sade.eperusteet.repository;

import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.validation.ValidointiStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ValidointiStatusRepository extends JpaRepository<ValidointiStatus, Long> {
    ValidointiStatus findOneByPeruste(Peruste peruste);
}
