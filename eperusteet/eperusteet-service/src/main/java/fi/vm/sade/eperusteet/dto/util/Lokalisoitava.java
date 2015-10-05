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

package fi.vm.sade.eperusteet.dto.util;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Collection;
import java.util.stream.Stream;

/**
 * User: tommiratamaa
 * Date: 5.10.15
 * Time: 18.52
 */
public interface Lokalisoitava {
    @JsonIgnore
    Stream<LokalisoituTekstiDto> lokalisoitavatTekstit();

    static Lokalisoitava of(LokalisoituTekstiDto... tekstit) {
        return () -> Stream.of(tekstit);
    }
    static Lokalisoitava of(Collection<? extends Lokalisoitava> of) {
        return () -> of.stream().flatMap(Lokalisoitava::lokalisoitavatTekstit);
    }
    default Lokalisoitava and(LokalisoituTekstiDto... teksti) {
        return () -> Stream.concat(lokalisoitavatTekstit(), Stream.of(teksti));
    }
    default Lokalisoitava and(Lokalisoitava... and) {
        return () -> Stream.concat(lokalisoitavatTekstit(),
                Stream.of(and).flatMap(Lokalisoitava::lokalisoitavatTekstit));
    }
    default Lokalisoitava and(Collection<? extends Lokalisoitava> and) {
        return () -> Stream.concat(lokalisoitavatTekstit(),
                and.stream().flatMap(Lokalisoitava::lokalisoitavatTekstit));
    }
}
