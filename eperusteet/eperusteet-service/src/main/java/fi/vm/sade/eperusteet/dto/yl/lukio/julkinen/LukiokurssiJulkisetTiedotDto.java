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

import fi.vm.sade.eperusteet.domain.yl.lukio.LukiokurssiTyyppi;
import fi.vm.sade.eperusteet.dto.util.Lokalisoitava;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.io.Serializable;
import java.util.UUID;
import java.util.stream.Stream;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto.localizeLaterById;
import static fi.vm.sade.eperusteet.dto.yl.lukio.julkinen.LokalisoitavaOsaDto.localizedLaterByIds;

/**
 * User: tommiratamaa
 * Date: 2.11.15
 * Time: 12.57
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LukiokurssiJulkisetTiedotDto implements Serializable, Lokalisoitava {
    private Long id;
    private Long oppiaineId;
    private Integer jarjestys;
    private UUID tunniste;
    private String koodiUri;
    private String koodiArvo;
    private LokalisoituTekstiDto lokalisoituKoodi;
    private LukiokurssiTyyppi tyyppi;
    private LokalisoituTekstiDto nimi;
    private LokalisoituTekstiDto kuvaus;
    private LokalisoitavaOsaDto tavoitteet;
    private LokalisoitavaOsaDto keskeisetSisallot;
    private LokalisoitavaOsaDto tavoitteetJaKeskeisetSisallot;

    public LukiokurssiJulkisetTiedotDto(Long id, Long oppiaineId, Integer jarjestys,
                                        UUID tunniste, String koodiUri, String koodiArvo,
                                        Long lokalisoituKoodiId, LukiokurssiTyyppi tyyppi,
                                        Long nimiId, Long kuvausId,
                                        Long tavoitteetOtsikkoId, Long tavoitteetTekstiId,
                                        Long sisallotOtsikkoId, Long sisallotTekstiId,
                                        Long tavoitteetJaSisallotOtsikkoId, Long tavoitteetJaSisallotTekstiId) {
        this.id = id;
        this.oppiaineId = oppiaineId;
        this.jarjestys = jarjestys;
        this.tunniste = tunniste;
        this.koodiUri = koodiUri;
        this.koodiArvo = koodiArvo;
        this.lokalisoituKoodi = localizeLaterById(lokalisoituKoodiId);
        this.tyyppi = tyyppi;
        this.nimi = localizeLaterById(nimiId);
        this.kuvaus = localizeLaterById(kuvausId);
        this.tavoitteet = localizedLaterByIds(tavoitteetOtsikkoId, tavoitteetTekstiId);
        this.keskeisetSisallot = localizedLaterByIds(sisallotOtsikkoId, sisallotTekstiId);
        this.tavoitteetJaKeskeisetSisallot = localizedLaterByIds(tavoitteetJaSisallotOtsikkoId, tavoitteetJaSisallotTekstiId);
    }

    @Override
    public Stream<LokalisoituTekstiDto> lokalisoitavatTekstit() {
        return Lokalisoitava.of(nimi, kuvaus, lokalisoituKoodi)
                .and(tavoitteet, keskeisetSisallot, tavoitteetJaKeskeisetSisallot)
                .lokalisoitavatTekstit();
    }
}
