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
import fi.vm.sade.eperusteet.dto.yl.lukio.LukiokurssiListausDto;
import fi.vm.sade.eperusteet.dto.yl.lukio.OppiaineKurssiHakuDto;
import fi.vm.sade.eperusteet.dto.yl.lukio.julkinen.LukiokurssiJulkisetTiedotDto;
import fi.vm.sade.eperusteet.repository.version.JpaWithVersioningRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * User: tommiratamaa
 * Date: 29.9.15
 * Time: 15.11
 */
public interface LukiokurssiRepository extends JpaWithVersioningRepository<Lukiokurssi, Long> {
    String KURSSILISTAUS_SELECT = "SELECT new fi.vm.sade.eperusteet.dto.yl.lukio.LukiokurssiListausDto(" +
            "   kurssi.id," +
            "   kurssi.tyyppi, " +
            "   kurssi.koodiArvo, kurssi.lokalisoituKoodi.id, " +
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
    List<LukiokurssiListausDto> findLukiokurssitByPerusteId(long perusteId);

    @Query(value = KURSSILISTAUS_SELECT + " FROM Lukiokurssi kurssi" +
            "   INNER JOIN kurssi.nimi nimi " +
            "   LEFT JOIN kurssi.kuvaus kuvaus " +
            "   INNER JOIN kurssi.oppiaineet kurssioppiaine ON kurssioppiaine.oppiaine.id = ?2 " +
            "   INNER JOIN kurssi.opetussuunnitelma os " +
            "   INNER JOIN os.sisalto sisalto ON sisalto.peruste.id = ?1 " +
            " ORDER BY kurssi.koodiArvo ")
    List<LukiokurssiListausDto> findLukiokurssitByPerusteAndOppiaineId(long perusteId, long oppiaineId);

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


    @Query(value = "SELECT new fi.vm.sade.eperusteet.dto.yl.lukio.julkinen.LukiokurssiJulkisetTiedotDto(\n" +
            "    kurssi.id, oa.id, koa.jarjestys,\n" +
            "    kurssi.tunniste, kurssi.koodiUri, kurssi.koodiArvo, kurssi.lokalisoituKoodi.id, kurssi.tyyppi,\n" +
            "    nimi.id, kuvaus.id,\n" +
            "    tavoitteet.otsikko.id, tavoitteet.teksti.id,\n" +
            "    sisallot.otsikko.id, sisallot.teksti.id,\n" +
            "    tavoitteetJaSisallot.otsikko.id, tavoitteetJaSisallot.teksti.id\n" +
            ") FROM Lukiokurssi kurssi\n" +
            "        INNER JOIN kurssi.nimi nimi\n" +
            "        LEFT JOIN kurssi.kuvaus kuvaus\n" +
            "        INNER JOIN kurssi.opetussuunnitelma os\n" +
            "        INNER JOIN os.sisalto sisalto ON sisalto.peruste.id = ?1\n" +
            "        INNER JOIN kurssi.oppiaineet koa\n" +
            "        INNER JOIN koa.oppiaine oa\n" +
            "        LEFT JOIN kurssi.tavoitteet tavoitteet\n" +
            "        LEFT JOIN kurssi.keskeinenSisalto sisallot\n" +
            "        LEFT JOIN kurssi.tavoitteetJaKeskeinenSisalto tavoitteetJaSisallot\n" +
            "ORDER BY oa.jnro, oa.koodiArvo, oa.id, koa.jarjestys, kurssi.koodiArvo, kurssi.id")
    List<LukiokurssiJulkisetTiedotDto> findLukiokurssiJulkinenDtosByPerusteId(long perusteId);
}
