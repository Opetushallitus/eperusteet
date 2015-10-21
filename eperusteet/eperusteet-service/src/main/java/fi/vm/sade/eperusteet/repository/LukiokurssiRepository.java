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

import fi.vm.sade.eperusteet.domain.yl.lukio.Lukiokurssi;
import fi.vm.sade.eperusteet.dto.yl.lukio.LukioKurssiListausDto;
import fi.vm.sade.eperusteet.dto.yl.lukio.OppiaineKurssiHakuDto;
import fi.vm.sade.eperusteet.repository.version.JpaWithVersioningRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * User: tommiratamaa
 * Date: 29.9.15
 * Time: 15.11
 */
public interface LukiokurssiRepository extends JpaWithVersioningRepository<Lukiokurssi, Long> {
    String KURSSILISTAUS_SELECT = "SELECT new fi.vm.sade.eperusteet.dto.yl.lukio.LukioKurssiListausDto(" +
            "   kurssi.id," +
            "   kurssi.tyyppi, " +
            "   kurssi.koodiArvo," +
            "   nimi.id," +
            "   kuvaus.id, " +
            "   kurssi.muokattu " +
            ")";

    @Query(value = KURSSILISTAUS_SELECT + " FROM Lukiokurssi kurssi" +
            "   INNER JOIN kurssi.nimi nimi " +
            "   LEFT JOIN kurssi.kuvaus kuvaus " +
            "   INNER JOIN kurssi.opetussuunnitelma os " +
            "   INNER JOIN os.sisalto sisalto ON sisalto.peruste.id = ?1 " +
            " ORDER BY kurssi.koodiArvo ")
    List<LukioKurssiListausDto> findLukiokurssitByPerusteId(long perusteId);

    @Query(value = KURSSILISTAUS_SELECT + " FROM Lukiokurssi kurssi" +
            "   INNER JOIN kurssi.nimi nimi " +
            "   LEFT JOIN kurssi.kuvaus kuvaus " +
            "   INNER JOIN kurssi.oppiaineet kurssioppiaine ON kurssioppiaine.oppiaine.id = ?2 " +
            "   INNER JOIN kurssi.opetussuunnitelma os " +
            "   INNER JOIN os.sisalto sisalto ON sisalto.peruste.id = ?1 " +
            " ORDER BY kurssi.koodiArvo ")
    List<LukioKurssiListausDto> findLukiokurssitByPerusteAndOppiaineId(long perusteId, long oppiaineId);

    @Query(value = "SELECT new fi.vm.sade.eperusteet.dto.yl.lukio.OppiaineKurssiHakuDto(" +
            "   aine.id,      " +
            "   kurssi.id, " +
            "   oalk.jarjestys," +
            "   aine.nimi.id " +
            ") FROM OppiaineLukiokurssi oalk" +
            "       INNER JOIN oalk.kurssi kurssi" +
            "       INNER JOIN kurssi.opetussuunnitelma os " +
            "       INNER JOIN os.sisalto sisalto " +
            "       INNER JOIN sisalto.peruste peruste" +
            "       INNER JOIN oalk.oppiaine aine" +
            " WHERE peruste.id = ?1 ORDER BY aine.jnro, aine.id, oalk.jarjestys, kurssi.id ")
    List<OppiaineKurssiHakuDto> findKurssiOppaineRelationsByPerusteId(long perusteId);

}
