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
import fi.vm.sade.eperusteet.dto.koodisto.KoodistoKoodiDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.KoodiDto;
import fi.vm.sade.eperusteet.dto.util.CombinedDto;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author isaul
 */
@Getter
@Setter
public class PerusteHakuDto extends PerusteDto {

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    List<KoodiDto> tutkintonimikeKoodit;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<CombinedDto<TutkintonimikeKoodiDto, HashMap<String, KoodistoKoodiDto>>> tutkintonimikkeetKoodisto = new ArrayList<>();

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<PerusteInfoDto> korvaavatPerusteet = new ArrayList<>();

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<PerusteInfoDto> korvattavatPerusteet = new ArrayList<>();
}
