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

package fi.vm.sade.eperusteet.dto.yl.lukio.julkinen;

import fi.vm.sade.eperusteet.dto.util.Lokalisoitava;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import lombok.Getter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto.localizeLaterById;
import static fi.vm.sade.eperusteet.dto.yl.lukio.julkinen.LokalisoitavaOsaDto.localizedLaterByIds;

/**
 * User: tommiratamaa
 * Date: 2.11.15
 * Time: 12.44
 */
@Getter
@Setter
@NoArgsConstructor
public class LukioOppiaineOppimaaraNodeDto implements Serializable, Lokalisoitava {
    private Long id;
    private Long parentId;
    private UUID tunniste;
    private Integer jarjestys;
    private String koodiArvo;
    private String koodiUri;
    private LokalisoituTekstiDto nimi;
    private boolean koosteinen;
    private Boolean abstrakti;

    private LokalisoituTekstiDto pakollinenKurssiKuvaus;
    private LokalisoituTekstiDto syventavaKurssiKuvaus;
    private LokalisoituTekstiDto soveltavaKurssiKuvaus;
    private LokalisoitavaOsaDto tavoitteet;
    private LokalisoitavaOsaDto arviointi;
    private LokalisoitavaOsaDto tehtava;

    private List<LukioOppiaineOppimaaraNodeDto> oppimaarat = new ArrayList<>();
    private List<LukiokurssiJulkisetTiedotDto> kurssit = new ArrayList<>();

    public LukioOppiaineOppimaaraNodeDto(Long id, Long parentId,
                                         UUID tunniste, Long nimiId, boolean koosteinen, Long jarjestys,
                                         String koodiArvo, String koodiUri, Boolean abstrakti,
                                         Long pakollinenKurssiKuvausId, Long syventavaKurssiKuvausId, Long soveltavaKurssiKuvausId,
                                         Long tavoitteetOtsikkoId, Long tavoitteetTekstiId,
                                         Long tehtavaOtsikkoId, Long tehtavaTekstiId,
                                         Long arviointiOtsikkoId, Long arviointiTekstiId) {
        this.id = id;
        this.parentId = parentId;
        this.tunniste = tunniste;
        this.jarjestys = jarjestys != null ? jarjestys.intValue() : null;
        this.koodiArvo = koodiArvo;
        this.koodiUri = koodiUri;
        this.nimi = localizeLaterById(nimiId);
        this.koosteinen = koosteinen;
        this.abstrakti = abstrakti;
        this.pakollinenKurssiKuvaus = localizeLaterById(pakollinenKurssiKuvausId);
        this.syventavaKurssiKuvaus = localizeLaterById(syventavaKurssiKuvausId);
        this.soveltavaKurssiKuvaus = localizeLaterById(soveltavaKurssiKuvausId);
        this.tavoitteet = localizedLaterByIds(tavoitteetOtsikkoId, tavoitteetTekstiId);
        this.arviointi = localizedLaterByIds(arviointiOtsikkoId, arviointiTekstiId);
        this.tehtava = localizedLaterByIds(tehtavaOtsikkoId, tehtavaTekstiId);
    }

    @Override
    public Stream<LokalisoituTekstiDto> lokalisoitavatTekstit() {
        return Lokalisoitava.of(nimi, pakollinenKurssiKuvaus, syventavaKurssiKuvaus, soveltavaKurssiKuvaus)
                .and(tavoitteet, arviointi).and(oppimaarat).and(kurssit).lokalisoitavatTekstit();
    }
}
