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

package fi.vm.sade.eperusteet.dto.yl.lukio;

import fi.vm.sade.eperusteet.domain.yl.lukio.LukiokurssiTyyppi;
import fi.vm.sade.eperusteet.dto.util.Lokalisoitava;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

import static fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto.localizeLaterById;

/**
 * User: tommiratamaa
 * Date: 29.9.15
 * Time: 15.51
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LukiokurssiListausDto implements Serializable, Lokalisoitava {
    private List<KurssinOppiaineNimettyDto> oppiaineet = new ArrayList<>();
    private Long id;
    private String koodiArvo;
    private LokalisoituTekstiDto lokalisoituKoodi;
    private LukiokurssiTyyppi tyyppi;
    private LokalisoituTekstiDto nimi;
    private LokalisoituTekstiDto kuvaus;
    private Date muokattu;

    public LukiokurssiListausDto(Long id, LukiokurssiTyyppi tyyppi,
                                 String koodiArvo, Long lokalisoituKoodiId,
                                 Long nimiId, Long kuvausId, Date muokattu) {
        this.id = id;
        this.tyyppi = tyyppi;
        this.koodiArvo = koodiArvo;
        this.lokalisoituKoodi = localizeLaterById(lokalisoituKoodiId);
        this.nimi = localizeLaterById(nimiId);
        this.kuvaus = localizeLaterById(kuvausId);
        this.muokattu = muokattu;
    }

    @Override
    public Stream<LokalisoituTekstiDto> lokalisoitavatTekstit() {
        return Lokalisoitava.of(nimi, lokalisoituKoodi, kuvaus).and(oppiaineet).lokalisoitavatTekstit();
    }
}
