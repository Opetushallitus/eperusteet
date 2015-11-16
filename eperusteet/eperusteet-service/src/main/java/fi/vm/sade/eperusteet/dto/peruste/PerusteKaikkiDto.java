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

package fi.vm.sade.eperusteet.dto.peruste;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import fi.vm.sade.eperusteet.dto.tutkinnonosa.TutkinnonOsaKaikkiDto;
import fi.vm.sade.eperusteet.dto.yl.EsiopetuksenPerusteenSisaltoDto;
import fi.vm.sade.eperusteet.dto.yl.PerusopetuksenPerusteenSisaltoDto;
import java.util.List;
import java.util.Set;

import fi.vm.sade.eperusteet.dto.yl.lukio.julkinen.LukiokoulutuksenPerusteenSisaltoDto;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author nkala
 */
@Getter
@Setter
public class PerusteKaikkiDto extends PerusteBaseDto {
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    Set<SuoritustapaLaajaDto> suoritustavat;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    List<TutkinnonOsaKaikkiDto> tutkinnonOsat;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("perusopetus")
    private PerusopetuksenPerusteenSisaltoDto perusopetuksenPerusteenSisalto;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("lukiokoulutus")
    private LukiokoulutuksenPerusteenSisaltoDto lukiokoulutuksenPerusteenSisalto;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("esiopetus")
    private EsiopetuksenPerusteenSisaltoDto esiopetuksenPerusteenSisalto;
}
