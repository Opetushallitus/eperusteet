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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import fi.vm.sade.eperusteet.domain.tutkinnonOsa.TutkinnonOsaTyyppi;
import fi.vm.sade.eperusteet.dto.tutkinnonOsa.TutkinnonOsaDto;
import fi.vm.sade.eperusteet.dto.util.EntityReference;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TutkinnonOsaViiteDto {

    private Long id;
    private BigDecimal laajuus;
    private Integer jarjestys;
    @JsonProperty("_tutkinnonOsa")
    private EntityReference tutkinnonOsa;
    @JsonProperty("tutkinnonOsa")
    private TutkinnonOsaDto tutkinnonOsaDto;

    private Date muokattu;
    private Boolean poistettu;
    private LokalisoituTekstiDto nimi;
    private TutkinnonOsaTyyppi tyyppi;

    public TutkinnonOsaViiteDto() {
        
    }

    public TutkinnonOsaViiteDto (BigDecimal laajuus, Integer jarjestys, LokalisoituTekstiDto nimi, TutkinnonOsaTyyppi tyyppi) {
        this.laajuus = laajuus;
        this.jarjestys = jarjestys;
        this.nimi = nimi;
        this.tyyppi = tyyppi;
    }
}
