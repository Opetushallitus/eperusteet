package fi.vm.sade.eperusteet.repository;

import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.validation.ValidointiStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ValidointiStatusRepository extends JpaRepository<ValidointiStatus, Long> {
    ValidointiStatus findOneByPeruste(Peruste peruste);

    @Query("SELECT vs FROM ValidointiStatus vs WHERE vs.vaihtoOk = FALSE OR vs.vaihtoOk IS NULL")
    Page<ValidointiStatus> findVirheelliset(Pageable pageable);
}
