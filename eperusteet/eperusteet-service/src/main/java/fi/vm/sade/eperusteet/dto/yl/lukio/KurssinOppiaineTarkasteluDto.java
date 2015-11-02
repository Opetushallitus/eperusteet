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

import fi.vm.sade.eperusteet.dto.util.Lokalisoitava;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import lombok.Getter;
import lombok.Setter;

import java.util.stream.Stream;

import static fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto.localizeLaterById;

/**
 * User: tommiratamaa
 * Date: 6.10.15
 * Time: 13.01
 */
@Getter
@Setter
public class KurssinOppiaineTarkasteluDto extends KurssinOppiaineNimettyDto {
    private OppiaineVanhempiDto vanhempi;
    private LokalisoituTekstiDto kurssiTyyppinKuvaus;

    public KurssinOppiaineTarkasteluDto() {
    }

    public KurssinOppiaineTarkasteluDto(Long oppiaineId, Integer jarjestys, Long oppiaineNimiId,
                                        OppiaineVanhempiDto vanhempi, Long kurssiTyypinKuvausId) {
        super(oppiaineId, jarjestys, oppiaineNimiId);
        this.vanhempi = vanhempi;
        this.kurssiTyyppinKuvaus = localizeLaterById(kurssiTyypinKuvausId);
    }

    @Override
    public Stream<LokalisoituTekstiDto> lokalisoitavatTekstit() {
        return Lokalisoitava.of(vanhempi).and(kurssiTyyppinKuvaus)
                .and(super.lokalisoitavatTekstit()).lokalisoitavatTekstit();
    }
}
