package fi.vm.sade.eperusteet.repository;

import fi.vm.sade.eperusteet.domain.PerusteTila;
import fi.vm.sade.eperusteet.domain.yl.lukio.LukiokoulutuksenPerusteenSisalto;
import org.springframework.data.jpa.repository.Query;

public interface LukiokoulutuksenPerusteenSisaltoRepository extends OppiaineSisaltoRepository<LukiokoulutuksenPerusteenSisalto> {

    @Query("SELECT peruste.tila\n" +
            "FROM LukiokoulutuksenPerusteenSisalto sisalto\n" +
            "    INNER JOIN sisalto.peruste peruste ON peruste.id = ?1")
    PerusteTila findLukioperusteenTilaByPerusteId(long perusteId);
}
