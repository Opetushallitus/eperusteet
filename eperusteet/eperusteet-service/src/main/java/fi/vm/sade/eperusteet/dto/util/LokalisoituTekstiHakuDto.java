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

import fi.vm.sade.eperusteet.domain.Kieli;
import lombok.Getter;
import lombok.Setter;

/**
 * User: tommiratamaa
 * Date: 5.10.15
 * Time: 20.20
 */
@Getter
@Setter
public class LokalisoituTekstiHakuDto {
    private long id;
    private Kieli kieli;
    private String teksti;

    public LokalisoituTekstiHakuDto() {
    }

    public LokalisoituTekstiHakuDto(long id, Kieli kieli, String teksti) {
        this.id = id;
        this.kieli = kieli;
        this.teksti = teksti;
    }
}
