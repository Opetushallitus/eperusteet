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

package fi.vm.sade.eperusteet.dto.yl;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * User: tommiratamaa
 * Date: 29.9.15
 * Time: 15.51
 */
@Getter
@Setter
public class LukiokurssiListausDto implements Serializable  {
    private List<JarjestettyOppiaineDto> oppiaineet = new ArrayList<>();
    private Long id;
    private String koodiArvo;
    private String nimi;
    private String kuvaus;
    private Date muokattu;

    public LukiokurssiListausDto() {
    }

    public LukiokurssiListausDto(Long id, String koodiArvo, String nimi, String kuvaus, Date muokattu) {
        this.id = id;
        this.koodiArvo = koodiArvo;
        this.nimi = nimi;
        this.kuvaus = kuvaus;
        this.muokattu = muokattu;
    }
}
