/*
 * Copyright (c) 2013 The Finnish Board of Education - Opetushallitus
 *
 * This program is free software: Licensed under the EUPL, Version 1.1 or - as
 * soon as they will be approved by the European Commission - subsequent versions
 * of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at: http://ec.europa.eu/idabc/eupl
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * European Union Public Licence for more details.
 */
package fi.vm.sade.eperusteet.repository;

import fi.vm.sade.eperusteet.domain.yl.OpetuksenKohdealue;
import fi.vm.sade.eperusteet.domain.yl.OpetuksenTavoite;
import fi.vm.sade.eperusteet.domain.yl.Oppiaine;
import fi.vm.sade.eperusteet.dto.yl.lukio.julkinen.LukioOppiaineOppimaaraNodeDto;
import fi.vm.sade.eperusteet.repository.version.JpaWithVersioningRepository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * @author jhyoty
 */
@Repository
public interface OppiaineRepository extends JpaWithVersioningRepository<Oppiaine, Long> {
    @Query("SELECT ot FROM OpetuksenTavoite ot WHERE ?1 MEMBER OF ot.kohdealueet")
    List<OpetuksenTavoite> findAllTavoitteetByKohdealue(OpetuksenKohdealue kohdealue);

    // NOTE:This only supports structures to one child level. In practice, this is enough for Lukio case.
    // Can only select Dtos/entities with JPA query and would require PostgreSQL query for recursive union.
    @Query("SELECT new fi.vm.sade.eperusteet.dto.yl.lukio.julkinen.LukioOppiaineOppimaaraNodeDto(\n" +
            "    oa.id, parent.id,\n" +
            "    oa.tunniste, oa.nimi.id, oa.koosteinen, oa.jnro,\n" +
            "    oa.koodiArvo, oa.koodiUri, oa.abstrakti,\n" +
            "    pakollinenKuvaus.id, syventavaKuvaus.id, soveltavaKuvaus.id,\n" +
            "    tavoitteet.otsikko.id, tavoitteet.teksti.id,\n" +
            "    tehtava.otsikko.id, tehtava.teksti.id,\n" +
            "    arviointi.otsikko.id, arviointi.teksti.id\n" +
            ") FROM Oppiaine oa\n" +
            "    LEFT JOIN oa.lukioRakenteet rakenne\n" +
            "    LEFT JOIN rakenne.sisalto sisalto\n" +
            "    LEFT JOIN sisalto.peruste peruste\n" +
            "    LEFT JOIN oa.oppiaine parent\n" +
            "    LEFT JOIN parent.lukioRakenteet parentRakenne\n" +
            "    LEFT JOIN parentRakenne.sisalto parentSisalto\n" +
            "    LEFT JOIN parentSisalto.peruste parentPeruste\n" +
            "    LEFT JOIN oa.pakollinenKurssiKuvaus pakollinenKuvaus\n" +
            "    LEFT JOIN oa.syventavaKurssiKuvaus syventavaKuvaus\n" +
            "    LEFT JOIN oa.soveltavaKurssiKuvaus soveltavaKuvaus\n" +
            "    LEFT JOIN oa.tavoitteet tavoitteet\n" +
            "    LEFT JOIN oa.arviointi arviointi\n" +
            "    LEFT JOIN oa.tehtava tehtava\n" +
            "WHERE (peruste.id = ?1 OR parentPeruste.id = ?1)\n" +
            "ORDER BY oa.jnro, oa.koodiArvo, oa.id")
    List<LukioOppiaineOppimaaraNodeDto> findLukioOppiaineetJulkinenDtosByPerusteId(long perusteId);
}
