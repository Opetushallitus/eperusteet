package fi.vm.sade.eperusteet.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import fi.vm.sade.eperusteet.domain.tutkinnonosa.TutkinnonOsa;
import fi.vm.sade.eperusteet.repository.version.JpaWithVersioningRepository;

@Repository
public interface TutkinnonOsaRepository extends JpaWithVersioningRepository<TutkinnonOsa, Long> {
    List<TutkinnonOsa> findByKoodiUri(String koodiUri);

    List<TutkinnonOsa> findByKoodiUriIn(List<String> koodiUris);

    List<TutkinnonOsa> findByNimiTekstiTekstiContainingIgnoreCase(String teksti);

    @Query("SELECT DISTINCT t FROM TutkinnonOsa t " +
            "LEFT JOIN t.geneerinenArviointiasteikko ga " +
            "LEFT JOIN t.osaAlueet oa " +
            "LEFT JOIN oa.geneerinenArviointiasteikko oaga " +
            "WHERE ga.id = :id OR oaga.id = :id")
    List<TutkinnonOsa> findByGeneerinenArviointiasteikkoKaytossa(@Param("id") Long geneerinenArviointiasteikkoId);

    @Query("SELECT to FROM Peruste p JOIN p.suoritustavat s JOIN s.tutkinnonOsat t JOIN t.tutkinnonOsa to WHERE to.koodi.uri = :koodiUri AND p.tila = 'VALMIS' ")
    List<TutkinnonOsa> findByKoodiUriAndValmiitPerusteet(@Param("koodiUri") String koodiUri);
}
