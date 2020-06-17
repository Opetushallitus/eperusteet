package fi.vm.sade.eperusteet.repository;

import fi.vm.sade.eperusteet.domain.JulkaistuPeruste;
import fi.vm.sade.eperusteet.domain.Peruste;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface JulkaisutRepository extends JpaRepository<JulkaistuPeruste, Long> {
    List<JulkaistuPeruste> findAllByPeruste(Peruste peruste);

    List<JulkaistuPeruste> findAllByPerusteOrderByRevisionDesc(@Param("peruste") Peruste peruste);

    long countByPeruste(Peruste peruste);

    JulkaistuPeruste findOneByPerusteAndLuotu(Peruste peruste, Date aikaleima);
}
