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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * User: tommiratamaa
 * Date: 2.11.15
 * Time: 12.43
 */
@Getter
@Setter
@NoArgsConstructor
public class LukioOppiainePuuDto implements Lokalisoitava {
    private Long perusteId;
    private List<LukioOppiaineOppimaaraNodeDto> oppiaineet = new ArrayList<>();

    public LukioOppiainePuuDto(Long perusteId) {
        this.perusteId = perusteId;
    }

    @Override
    public Stream<LokalisoituTekstiDto> lokalisoitavatTekstit() {
        return Lokalisoitava.of(oppiaineet).lokalisoitavatTekstit();
    }
}
