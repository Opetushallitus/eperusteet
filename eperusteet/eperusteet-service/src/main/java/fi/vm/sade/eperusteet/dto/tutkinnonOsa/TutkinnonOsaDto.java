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

package fi.vm.sade.eperusteet.dto.tutkinnonOsa;

import com.fasterxml.jackson.annotation.JsonTypeName;
import fi.vm.sade.eperusteet.domain.tutkinnonOsa.TutkinnonOsaTyyppi;
import fi.vm.sade.eperusteet.dto.Arviointi.ArviointiDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author jhyoty
 */
@Getter
@Setter
@JsonTypeName("tutkinnonosa")
public class TutkinnonOsaDto extends PerusteenOsaDto.Laaja {
    private LokalisoituTekstiDto tavoitteet;
    private ArviointiDto arviointi;
    private LokalisoituTekstiDto ammattitaitovaatimukset;
    private LokalisoituTekstiDto ammattitaidonOsoittamistavat;
    private LokalisoituTekstiDto kuvaus;
    private Long opintoluokitus;
    private String koodiUri;
    private String koodiArvo;
    private List<OsaAlueDto> osaAlueet;
    private TutkinnonOsaTyyppi tyyppi;
}
