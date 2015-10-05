/*
 *  Copyright (c) 2013 The Finnish Board of Education - Opetushallitus
 *
 *  This program is free software: Licensed under the EUPL, Version 1.1 or - as
 *  soon as they will be approved by the European Commission - subsequent versions
 *  of the EUPL (the "Licence");
 *
 *  You may not use this work except in compliance with the Licence.
 *  You may obtain a copy of the Licence at: http://ec.europa.eu/idabc/eupl
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  European Union Public Licence for more details.
 */

package fi.vm.sade.eperusteet.repository;

import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.yl.lukio.Aihekokonaisuus;
import fi.vm.sade.eperusteet.dto.lukiokoulutus.AihekokonaisuusListausDto;
import fi.vm.sade.eperusteet.dto.yl.LukiokurssiListausDto;
import fi.vm.sade.eperusteet.repository.version.JpaWithVersioningRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * User: jsikio
 */
public interface LukioAihekokonaisuusRepository extends JpaWithVersioningRepository<Aihekokonaisuus, Long> {

    @Query(value = "SELECT new fi.vm.sade.eperusteet.dto.lukiokoulutus.AihekokonaisuusListausDto(" +
            "   aihekokonaisuus.id," +
            "   otsikkoTeksti.teksti," +
            "   kuvausTeksti.teksti, " +
            "   aihekokonaisuus.jnro, " +
            "   aihekokonaisuus.muokattu " +
            ") FROM Aihekokonaisuus aihekokonaisuus" +
            "   INNER JOIN aihekokonaisuus.otsikko otsikko " +
            "   LEFT JOIN otsikko.teksti otsikkoTeksti ON otsikkoTeksti.kieli = ?2 " +
            "   LEFT JOIN aihekokonaisuus.yleiskuvaus kuvaus " +
            "   LEFT JOIN kuvaus.teksti kuvausTeksti ON kuvausTeksti.kieli = ?2 " +
            "   INNER JOIN aihekokonaisuus.aihekokonaisuudet aihekokonaisuudet " +
            "   INNER JOIN aihekokonaisuudet.sisalto sisalto ON sisalto.peruste.id = ?1 " +
            " ORDER BY aihekokonaisuus.jnro, otsikkoTeksti.teksti")
    List<AihekokonaisuusListausDto> findAihekokonaisuudetByPerusteId(long perusteId, Kieli kieli);


}
