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
import fi.vm.sade.eperusteet.domain.tutkinnonosa.TutkinnonOsaTyyppi;
import fi.vm.sade.eperusteet.dto.ReferenceableDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaDto;
import fi.vm.sade.eperusteet.dto.tutkinnonosa.AbstractTutkinnonOsaDto;
import fi.vm.sade.eperusteet.dto.tutkinnonosa.TutkinnonOsaDto;
import fi.vm.sade.eperusteet.dto.util.EntityReference;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import java.math.BigDecimal;
import java.util.Date;

import lombok.*;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class TutkinnonOsaViiteDto<T extends AbstractTutkinnonOsaDto> implements ReferenceableDto {

    private Long id;
    private BigDecimal laajuus;
    private BigDecimal laajuusMaksimi;
    private Integer jarjestys;

    @JsonProperty("_tutkinnonOsa")
    private EntityReference tutkinnonOsa;

    @JsonProperty("tutkinnonOsa")
    private T tutkinnonOsaDto;

    private Date muokattu;
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
