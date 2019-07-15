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
package fi.vm.sade.eperusteet.dto.tutkinnonrakenne;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.Map;

/**
 *
 * @author nkala
 */
@Getter
@Setter
@EqualsAndHashCode(of = {"koodisto", "uri", "versio"})
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class KoodiDto {
    private Map<String, String> nimi;
    private String arvo;
    private String uri;
    private String koodisto;
    private Long versio;

    static public KoodiDto of(String koodisto, String arvo) {
        KoodiDto result = new KoodiDto();
        result.setUri(koodisto + "_" + arvo);
        result.setKoodisto(koodisto);
        result.setArvo(arvo);
        return result;
    }
}
