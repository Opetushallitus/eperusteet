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

package fi.vm.sade.eperusteet.dto.koodisto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import fi.vm.sade.eperusteet.service.mapping.Koodisto;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author harrik
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class KoodistoDto {
    private String koodistoUri;

    public static KoodistoDto of(String uri) {
        KoodistoDto result = new KoodistoDto();
        result.setKoodistoUri(uri);
        return result;
    }
}
