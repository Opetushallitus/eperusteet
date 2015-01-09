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
package fi.vm.sade.eperusteet.dto.yl;

import com.google.common.base.Optional;
import fi.vm.sade.eperusteet.domain.yl.Vuosiluokka;
import fi.vm.sade.eperusteet.dto.ReferenceableDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author jhyoty
 */
@Getter
@Setter
public class VuosiluokkaKokonaisuusDto implements ReferenceableDto {
    private Long id;
    private Set<Vuosiluokka> vuosiluokat;
    private Optional<LokalisoituTekstiDto> nimi;
    private Optional<TekstiOsaDto> siirtymaEdellisesta;
    private Optional<TekstiOsaDto> tehtava;
    private Optional<TekstiOsaDto> siirtymaSeuraavaan;
    private Optional<TekstiOsaDto> laajaalainenOsaaminen;
    private Set<VuosiluokkaKokonaisuudenLaajaalainenOsaaminenDto> laajaalaisetOsaamiset;
    private Optional<TekstiOsaDto> paikallisestiPaatettavatAsiat;
}
