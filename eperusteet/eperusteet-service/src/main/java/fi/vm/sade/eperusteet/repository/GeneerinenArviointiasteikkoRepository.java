package fi.vm.sade.eperusteet.repository;

import fi.vm.sade.eperusteet.domain.GeneerinenArviointiasteikko;
import fi.vm.sade.eperusteet.repository.version.JpaWithVersioningRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface GeneerinenArviointiasteikkoRepository extends JpaWithVersioningRepository<GeneerinenArviointiasteikko, Long> {

    @Query(nativeQuery = true,
            value = "SELECT CASE WHEN count(*) > 0 THEN true ELSE false END " +
                    "FROM geneerinenArviointiasteikko_aud " +
                    "WHERE julkaistu = true AND id = :id")
    boolean auditJulkaistu(@Param("id") Long id);
}
