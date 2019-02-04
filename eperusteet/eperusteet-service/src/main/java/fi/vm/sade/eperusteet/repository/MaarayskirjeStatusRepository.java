package fi.vm.sade.eperusteet.repository;

import fi.vm.sade.eperusteet.domain.MaarayskirjeStatus;
import fi.vm.sade.eperusteet.domain.Peruste;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface MaarayskirjeStatusRepository extends JpaRepository<MaarayskirjeStatus, Long> {
    MaarayskirjeStatus findOneByPeruste(Peruste peruste);

    @Query("SELECT vs FROM MaarayskirjeStatus vs WHERE vs.lataaminenOk = FALSE OR vs.lataaminenOk IS NULL")
    Page<MaarayskirjeStatus> findLataamattomat(Pageable pageable);
}
