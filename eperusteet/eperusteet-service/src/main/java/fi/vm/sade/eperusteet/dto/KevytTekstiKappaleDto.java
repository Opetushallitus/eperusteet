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
package fi.vm.sade.eperusteet.dto;

import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 *
 * @author nkala
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KevytTekstiKappaleDto {
    private Long id;
    private LokalisoituTekstiDto nimi;
    private LokalisoituTekstiDto teksti;
    private Integer jnro;

    static public KevytTekstiKappaleDto of(LokalisoituTekstiDto nimi, LokalisoituTekstiDto teksti) {
        KevytTekstiKappaleDto result = new KevytTekstiKappaleDto();
        result.setNimi(nimi);
        result.setTeksti(teksti);
        return result;
    }

    static public KevytTekstiKappaleDto of(String nimi, String teksti) {
        KevytTekstiKappaleDto result = new KevytTekstiKappaleDto();
        result.setNimi(LokalisoituTekstiDto.of(nimi));
        result.setTeksti(LokalisoituTekstiDto.of(teksti));
        return result;
    }
}
