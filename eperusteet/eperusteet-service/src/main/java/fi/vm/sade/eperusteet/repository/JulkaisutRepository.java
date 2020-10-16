package fi.vm.sade.eperusteet.repository;

import fi.vm.sade.eperusteet.domain.JulkaistuPeruste;
import fi.vm.sade.eperusteet.domain.Peruste;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Set;

@Repository
public interface JulkaisutRepository extends JpaRepository<JulkaistuPeruste, Long> {
    List<JulkaistuPeruste> findAllByPeruste(Peruste peruste);

    List<JulkaistuPeruste> findAllByPerusteId(Long id);

    List<JulkaistuPeruste> findAllByPerusteOrderByRevisionDesc(@Param("peruste") Peruste peruste);

    JulkaistuPeruste findFirstByPerusteOrderByRevisionDesc(@Param("peruste") Peruste peruste);

    long countByPeruste(Peruste peruste);

    JulkaistuPeruste findOneByPerusteAndLuotu(Peruste peruste, Date aikaleima);

    @Query("SELECT p " +
            "FROM JulkaistuPeruste jp " +
            "    INNER JOIN jp.peruste p " +
            "WHERE p.tyyppi = 'NORMAALI' " +
            "   AND p.koulutustyyppi IN ('koulutustyyppi_1', 'koulutustyyppi_11', 'koulutustyyppi_12', 'koulutustyyppi_5', 'koulutustyyppi_18', 'koulutustyyppi_30') " +
            "   AND (p.voimassaoloLoppuu IS NULL " +
            "       OR p.voimassaoloLoppuu > NOW() " +
            "       OR (p.siirtymaPaattyy IS NOT NULL " +
            "           AND p.siirtymaPaattyy > NOW()))")
    Set<Peruste> findAmosaaJulkaisut();
}
