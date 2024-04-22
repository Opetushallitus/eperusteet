package fi.vm.sade.eperusteet.repository;

import fi.vm.sade.eperusteet.domain.yl.lukio.Aihekokonaisuus;
import fi.vm.sade.eperusteet.dto.yl.lukio.AihekokonaisuusListausDto;
import fi.vm.sade.eperusteet.repository.version.JpaWithVersioningRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface LukioAihekokonaisuusRepository extends JpaWithVersioningRepository<Aihekokonaisuus, Long> {

    @Query(value = "SELECT new fi.vm.sade.eperusteet.dto.yl.lukio.AihekokonaisuusListausDto(" +
            "   aihekokonaisuus.id," +
            "   otsikko.id," +
            "   kuvaus.id, " +
            "   aihekokonaisuus.jnro, " +
            "   aihekokonaisuus.muokattu " +
            ") FROM Aihekokonaisuus aihekokonaisuus" +
            "   INNER JOIN aihekokonaisuus.otsikko otsikko " +
            "   LEFT JOIN aihekokonaisuus.yleiskuvaus kuvaus " +
            "   INNER JOIN aihekokonaisuus.aihekokonaisuudet aihekokonaisuudet " +
            "   INNER JOIN aihekokonaisuudet.sisalto sisalto ON sisalto.peruste.id = ?1 " +
            " ORDER BY aihekokonaisuus.jnro")
    List<AihekokonaisuusListausDto> findAihekokonaisuudetByPerusteId(long perusteId);

}
