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
import lombok.Getter;

import java.io.Serializable;
import java.util.UUID;
import java.util.stream.Stream;

import static fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto.localizeLaterById;
import static fi.vm.sade.eperusteet.dto.yl.lukio.julkinen.LokalisoitavaOsaDto.localizedLaterByIds;

/**
 * User: tommiratamaa
 * Date: 2.11.15
 * Time: 12.57
 */
@Getter
public class LukiokurssiJulkisetTiedotDto implements Serializable, Lokalisoitava {
    private final Long id;
    private final Long oppiaineId;
    private final Integer jarjestys;
    private final UUID tunniste;
    private final String koodiUri;
    private final String koodiArvo;
    private final LukiokurssiTyyppi tyyppi;
    private final LokalisoituTekstiDto nimi;
    private final LokalisoituTekstiDto kuvaus;
    private final LokalisoitavaOsaDto tavoitteet;
    private final LokalisoitavaOsaDto keskeisetSisallot;
    private final LokalisoitavaOsaDto tavoitteetJaKeskeisetSisallot;

    public LukiokurssiJulkisetTiedotDto(Long id, Long oppiaineId, Integer jarjestys,
                                        UUID tunniste, String koodiUri, String koodiArvo, LukiokurssiTyyppi tyyppi,
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
        this.tyyppi = tyyppi;
        this.nimi = localizeLaterById(nimiId);
        this.kuvaus = localizeLaterById(kuvausId);
        this.tavoitteet = localizedLaterByIds(tavoitteetOtsikkoId, tavoitteetTekstiId);
        this.keskeisetSisallot = localizedLaterByIds(sisallotOtsikkoId, sisallotTekstiId);
        this.tavoitteetJaKeskeisetSisallot = localizedLaterByIds(tavoitteetJaSisallotOtsikkoId, tavoitteetJaSisallotTekstiId);
    }

    @Override
    public Stream<LokalisoituTekstiDto> lokalisoitavatTekstit() {
        return Lokalisoitava.of(nimi, kuvaus).and(tavoitteet, keskeisetSisallot, tavoitteetJaKeskeisetSisallot)
                .lokalisoitavatTekstit();
    }
}
