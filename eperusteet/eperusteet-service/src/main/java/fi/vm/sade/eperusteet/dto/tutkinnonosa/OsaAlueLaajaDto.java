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

package fi.vm.sade.eperusteet.dto.tutkinnonosa;

import java.util.List;

import fi.vm.sade.eperusteet.dto.Arviointi2020Dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 *
 * @author harrik
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OsaAlueLaajaDto extends OsaAlueDto {
    private Arviointi2020Dto arviointi;
    private Osaamistavoite2020Dto pakollisetOsaamistavoitteet;
    private Osaamistavoite2020Dto valinnaisetOsaamistavoitteet;

    @Deprecated
    private List<OsaamistavoiteDto> osaamistavoitteet;
}
